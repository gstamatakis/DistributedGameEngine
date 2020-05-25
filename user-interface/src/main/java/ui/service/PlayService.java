package ui.service;

import com.google.gson.Gson;
import message.DefaultKafkaMessage;
import message.completed.CompletedMoveMessage;
import message.completed.CompletedPlayMessage;
import message.created.MoveMessage;
import message.created.PlayMessage;
import message.queue.CreateTournamentQueueMessage;
import message.queue.JoinTournamentQueueMessage;
import message.queue.PracticeQueueMessage;
import message.requests.RequestCreateTournamentMessage;
import message.requests.RequestPracticeMessage;
import model.GameTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import ui.model.PlayEntity;
import ui.repository.PlayRepository;
import websocket.DefaultSTOMPMessage;
import websocket.STOMPMessageType;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class PlayService {
    private static final Logger logger = LoggerFactory.getLogger(PlayService.class);
    private final String joinPlayTopic = "join-play";
    private final String newMovesTopic = "new-moves";
    private final String newPlaysTopic = "new-plays";
    private final String ongoingPlaysTopic = "ongoing-plays";
    private final String completedPlaysTopic = "completed-plays";
    private final String completedMovesTopic = "completed-moves";

    private final Gson gson = new Gson();
    private final long timeout = 5;

    @Autowired
    private KafkaTemplate<String, DefaultKafkaMessage> kafkaMessageTemplate;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private PlayRepository playRepository;


    public void enqueuePractice(String username, RequestPracticeMessage reqMsg) throws InterruptedException, ExecutionException, TimeoutException {
        PracticeQueueMessage newMsg = new PracticeQueueMessage(username, reqMsg);
        kafkaMessageTemplate
                .send(joinPlayTopic, username, new DefaultKafkaMessage(newMsg, PracticeQueueMessage.class.getCanonicalName()))
                .get(timeout, TimeUnit.SECONDS);
        logger.info(String.format("Enqueued [%s] for a Practice Play with message [%s].", username, newMsg.toString()));
    }

    //Create a tournament ID
    //Use the tournament ID as the key
    public void createTournament(String username, RequestCreateTournamentMessage msg) throws IllegalStateException, InterruptedException, ExecutionException, TimeoutException {
        //Create the tournamentID by hashing part of the input
        GameTypeEnum gameType = msg.getTournamentGameType();
        String tournamentID = msg.getTournamentID();
        Set<String> blacklist = msg.getBlackList();
        int numOfParticipants = msg.getNumOfParticipants();

        //Make sure the number of participants is valid
        if (numOfParticipants < 4 || numOfParticipants % 2 != 0) {
            throw new IllegalStateException("Number of participants must be at least 4 and a multiple of 2!");
        }

        //Check the database to ensure a unique tournament ID
        if (playRepository.existsByPlayID(tournamentID)) {
            throw new IllegalStateException("Tournament ID already exists!");
        }
        playRepository.save(new PlayEntity(tournamentID));

        //Message
        CreateTournamentQueueMessage message = new CreateTournamentQueueMessage(username, gameType, blacklist, numOfParticipants, tournamentID);

        //Send the message
        kafkaMessageTemplate
                .send(joinPlayTopic, tournamentID, new DefaultKafkaMessage(message, CreateTournamentQueueMessage.class.getCanonicalName()))
                .get(timeout, TimeUnit.SECONDS);
        logger.info(String.format("Enqueued [%s] for a Tournament Play with id=[%s]", username, tournamentID));
    }

    //Queue a user for a tournament
    public void joinTournament(String username, String tournamentID) throws InterruptedException, ExecutionException, TimeoutException {
        JoinTournamentQueueMessage newMsg = new JoinTournamentQueueMessage(username, tournamentID);
        kafkaMessageTemplate
                .send(joinPlayTopic, tournamentID, new DefaultKafkaMessage(newMsg, JoinTournamentQueueMessage.class.getCanonicalName()))
                .get(timeout, TimeUnit.SECONDS);
        logger.info(String.format("User [%s] joined the tournament with id=[%s]", username, tournamentID));
    }

    //Send new move to play
    public void sendMoveToPlay(String sentBy, String move, String playID) throws InterruptedException, ExecutionException, TimeoutException {
        MoveMessage newMsg = new MoveMessage(sentBy, move, playID);
        kafkaMessageTemplate
                .send(newMovesTopic, playID, new DefaultKafkaMessage(newMsg, MoveMessage.class.getCanonicalName()))
                .get(timeout, TimeUnit.SECONDS);
        logger.info(String.format("User [%s] played move [%s]", sentBy, move));
    }


    @KafkaListener(topics = newPlaysTopic, containerFactory = "kafkaDefaultListenerContainerFactory")
    public void listenForNewPlays(@Payload String message,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
        PlayMessage newPlay = (PlayMessage) gson.fromJson(message, DefaultKafkaMessage.class).retrieve(PlayMessage.class.getCanonicalName());
        String playID = newPlay.getID();
        logger.info("Received new play message: " + newPlay.toString() + " from partition " + partition);

        //Alert the 2 players that their game has started
        //P1
        messagingTemplate.convertAndSendToUser(newPlay.getP1(), "/queue/reply", new DefaultSTOMPMessage(
                newPlay.getP1(),
                newPlay.getP2(),
                STOMPMessageType.GAME_START,
                null,
                playID));

        //P2
        messagingTemplate.convertAndSendToUser(newPlay.getP2(), "/queue/reply",
                new DefaultSTOMPMessage(
                        newPlay.getP2(),
                        newPlay.getP1(),
                        STOMPMessageType.GAME_START,
                        null,
                        playID));
    }

    @KafkaListener(topics = ongoingPlaysTopic, containerFactory = "kafkaDefaultListenerContainerFactory")
    public void listenOngoingPlays(@Payload String message,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
        //Convert the new play to an object
        PlayMessage ongoingPlay = (PlayMessage) gson.fromJson(message, DefaultKafkaMessage.class).retrieve(PlayMessage.class.getCanonicalName());
        String playID = ongoingPlay.getID();
        logger.info("Received ongoing play message: " + ongoingPlay.toString() + " from partition " + partition);

        //Alert the 2 users that they either need to move or wait for the opponent
        //P1
        messagingTemplate.convertAndSendToUser(ongoingPlay.getP1(), "/queue/reply",
                new DefaultSTOMPMessage(
                        ongoingPlay.getP1(),
                        String.format("You need to make a move %s: ", ongoingPlay.getP1()),
                        STOMPMessageType.NEED_TO_MOVE,
                        null,
                        playID));

        //P2
        messagingTemplate.convertAndSendToUser(ongoingPlay.getP2(), "/queue/reply", new DefaultSTOMPMessage(
                ongoingPlay.getP2(),
                String.format("Waiting for %s to make a move.", ongoingPlay.getP1()),
                STOMPMessageType.NOTIFICATION,
                null,
                playID));
    }

    @KafkaListener(topics = completedPlaysTopic, containerFactory = "kafkaDefaultListenerContainerFactory")
    public void listenForCompletedPlays(@Payload String message,
                                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) throws InterruptedException, ExecutionException, TimeoutException {
        CompletedPlayMessage completedPlayMessage = (CompletedPlayMessage) gson.fromJson(message, DefaultKafkaMessage.class).retrieve(CompletedPlayMessage.class.getCanonicalName());
        playRepository.save(new PlayEntity(completedPlayMessage));
        logger.info("Received completed play message: " + completedPlayMessage.toString() + " from partition " + partition);

        messagingTemplate.convertAndSendToUser(completedPlayMessage.getP1(), "/queue/reply",
                new DefaultSTOMPMessage(
                        completedPlayMessage.getP1(),
                        String.format("You *WON* against %s", completedPlayMessage.getP2()),
                        STOMPMessageType.GAME_OVER,
                        null,
                        completedPlayMessage.getPlayID()));

        messagingTemplate.convertAndSendToUser(completedPlayMessage.getP2(), "/queue/reply",
                new DefaultSTOMPMessage(
                        completedPlayMessage.getP2(),
                        String.format("You *LOST* against %s", completedPlayMessage.getP1()),
                        STOMPMessageType.GAME_OVER,
                        null,
                        completedPlayMessage.getPlayID()));

        //Send a Tombstone message to the ongoing-plays topic to remove the finished play
        //Make sure necessary info like scores is kept safe
        kafkaMessageTemplate
                .send(ongoingPlaysTopic, completedPlayMessage.getPlayID(), null)
                .get(timeout, TimeUnit.SECONDS);
    }

    @KafkaListener(topics = completedMovesTopic, containerFactory = "kafkaDefaultListenerContainerFactory")
    public void listenForCompletedMoves(@Payload String message,
                                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
        CompletedMoveMessage completedMoveMessage = (CompletedMoveMessage) gson.fromJson(message, DefaultKafkaMessage.class).retrieve(CompletedMoveMessage.class.getCanonicalName());
        logger.info("Received completed move message: " + completedMoveMessage.toString() + " from partition " + partition);
        if (completedMoveMessage.isValid()) {
            messagingTemplate.convertAndSendToUser(completedMoveMessage.getPlayedByUsername(), "/queue/reply",
                    new DefaultSTOMPMessage(
                            completedMoveMessage.getPlayedByUsername(),
                            message,
                            STOMPMessageType.MOVE_ACCEPTED,
                            null,
                            completedMoveMessage.getMoveMessage().getPlayID()));
            messagingTemplate.convertAndSendToUser(completedMoveMessage.getOpponentUsername(), "/queue/reply",
                    new DefaultSTOMPMessage(
                            completedMoveMessage.getPlayedByUsername(),
                            message,
                            STOMPMessageType.NEW_MOVE,
                            null,
                            completedMoveMessage.getMoveMessage().getPlayID()));
        } else {
            if (completedMoveMessage.getPlayedByUsername() == null) {
                logger.error(String.format("Invalid move message can't be sent back due to null username [%s]", completedMoveMessage.toString()));
                return;
            }
            messagingTemplate.convertAndSendToUser(completedMoveMessage.getPlayedByUsername(), "/queue/reply",
                    new DefaultSTOMPMessage(
                            completedMoveMessage.getPlayedByUsername(),
                            message,
                            STOMPMessageType.MOVE_DENIED,
                            null,
                            completedMoveMessage.getMoveMessage().getPlayID()));

        }
    }
}
