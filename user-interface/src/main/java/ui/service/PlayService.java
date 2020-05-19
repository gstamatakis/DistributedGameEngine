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
    private final String newPlaysTopic = "new-plays";
    private final String newMovesTopic = "new-moves";
    private final String completedPlaysTopic = "completed-plays";
    private final String completedMovesTopic = "completed-moves";
    private final String errorsTopic = "errors";

    private final Gson gson = new Gson();
    private final long timeout = 5;

    @Autowired
    private KafkaTemplate<String, DefaultKafkaMessage> kafkaJoinQueueTemplate;

    @Autowired
    private PlayRepository playRepository;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    public void enqueuePractice(String username, RequestPracticeMessage reqMsg) throws InterruptedException, ExecutionException, TimeoutException {
        kafkaJoinQueueTemplate
                .send(joinPlayTopic, username, new DefaultKafkaMessage(new PracticeQueueMessage(username, reqMsg)))
                .get(timeout, TimeUnit.SECONDS);
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
        kafkaJoinQueueTemplate
                .send(joinPlayTopic, tournamentID, new DefaultKafkaMessage(message))
                .get(timeout, TimeUnit.SECONDS);
    }

    //Queue a user for a tournament
    public void joinTournament(String username, String tournamentID) throws InterruptedException, ExecutionException, TimeoutException {
        kafkaJoinQueueTemplate
                .send(joinPlayTopic, tournamentID, new DefaultKafkaMessage(new JoinTournamentQueueMessage(username, tournamentID)))
                .get(timeout, TimeUnit.SECONDS);
    }

    //Send new move to play
    public void sendMoveToPlay(String sentBy, String move, String playID) throws InterruptedException, ExecutionException, TimeoutException {
        kafkaJoinQueueTemplate
                .send(newMovesTopic, playID, new DefaultKafkaMessage(new MoveMessage(sentBy, move, playID)))
                .get(timeout, TimeUnit.SECONDS);
    }

    @KafkaListener(topics = newPlaysTopic, containerFactory = "kafkaDefaultListenerContainerFactory")
    public void listenForNewPlays(@Payload String message,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                  @Header(KafkaHeaders.OFFSET) int offset) {
        PlayMessage newPlay = gson.fromJson(message, PlayMessage.class);
        String playID = newPlay.getID();
        logger.info("Received new play message: " + newPlay.toString() + " from partition " + partition);

        //Alert the 2 players that their game has started
        //P1
        messagingTemplate.convertAndSendToUser(newPlay.getP1(), "/queue/reply", new DefaultSTOMPMessage(
                newPlay.getP1(),
                String.format("You have been matched with player %s.", newPlay.getP2()),
                STOMPMessageType.GAME_START,
                playID,
                null));

        messagingTemplate.convertAndSendToUser(newPlay.getP1(), "/queue/reply",
                new DefaultSTOMPMessage(
                        newPlay.getP1(),
                        String.format("You need to make a move %s.", newPlay.getP1()),
                        STOMPMessageType.NEED_TO_MOVE,
                        playID,
                        null));

        //P2
        messagingTemplate.convertAndSendToUser(newPlay.getP2(), "/queue/reply",
                new DefaultSTOMPMessage(
                        newPlay.getP2(),
                        String.format("You have been matched with player %s.", newPlay.getP1()),
                        STOMPMessageType.GAME_START,
                        playID,
                        null));

        messagingTemplate.convertAndSendToUser(newPlay.getP2(), "/queue/reply", new DefaultSTOMPMessage(
                newPlay.getP2(),
                String.format("Waiting for %s to make a move.", newPlay.getP1()),
                STOMPMessageType.NOTIFICATION,
                playID,
                null));

    }

    @KafkaListener(topics = completedPlaysTopic, containerFactory = "kafkaDefaultListenerContainerFactory")
    public void listenForFinishedPlays(@Payload String message,
                                       @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                       @Header(KafkaHeaders.OFFSET) int offset) {
        CompletedPlayMessage completedPlayMessage = gson.fromJson(message, DefaultKafkaMessage.class).getCompletedPlayMessage();
        playRepository.save(new PlayEntity(completedPlayMessage));
        logger.info("Received completed play message: " + completedPlayMessage.toString() + " from partition " + partition);

        messagingTemplate.convertAndSendToUser(completedPlayMessage.getWinnerPlayer(), "/queue/reply",
                new DefaultSTOMPMessage(
                        completedPlayMessage.getWinnerPlayer(),
                        String.format("You *WON* against %s", completedPlayMessage.getLoserPlayer()),
                        STOMPMessageType.GAME_OVER,
                        completedPlayMessage.getPlayID(),
                        null));

        messagingTemplate.convertAndSendToUser(completedPlayMessage.getLoserPlayer(), "/queue/reply",
                new DefaultSTOMPMessage(
                        completedPlayMessage.getLoserPlayer(),
                        String.format("You *LOST* against %s", completedPlayMessage.getWinnerPlayer()),
                        STOMPMessageType.GAME_OVER,
                        completedPlayMessage.getPlayID(),
                        null));

    }

    @KafkaListener(topics = completedMovesTopic, containerFactory = "kafkaDefaultListenerContainerFactory")
    public void listenForFinishedMoves(@Payload String message,
                                       @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                       @Header(KafkaHeaders.OFFSET) int offset) {
        CompletedMoveMessage completedMoveMessage = gson.fromJson(message, DefaultKafkaMessage.class).getCompletedMoveMessage();
        logger.info("Received completed move message: " + completedMoveMessage.toString() + " from partition " + partition);
        if (completedMoveMessage.isValid()) {
            messagingTemplate.convertAndSendToUser(completedMoveMessage.getPlayedByUsername(), "/queue/reply",
                    new DefaultSTOMPMessage(
                            completedMoveMessage.getPlayedByUsername(),
                            message,
                            STOMPMessageType.MOVE_ACCEPTED,
                            completedMoveMessage.getMoveMessage().getPlayID(),
                            null));
            messagingTemplate.convertAndSendToUser(completedMoveMessage.getOpponentUsername(), "/queue/reply",
                    new DefaultSTOMPMessage(
                            completedMoveMessage.getPlayedByUsername(),
                            message,
                            STOMPMessageType.NEW_MOVE,
                            completedMoveMessage.getMoveMessage().getPlayID(),
                            null));
        } else {
            messagingTemplate.convertAndSendToUser(completedMoveMessage.getPlayedByUsername(), "/queue/reply",
                    new DefaultSTOMPMessage(
                            completedMoveMessage.getPlayedByUsername(),
                            message,
                            STOMPMessageType.MOVE_DENIED,
                            completedMoveMessage.getMoveMessage().getPlayID(),
                            null));

        }
    }

    @KafkaListener(topics = errorsTopic, containerFactory = "kafkaListenerContainerFactory")
    public void listenForErrors(@Payload String message) {
        logger.info("Received error message: " + message);
        messagingTemplate.convertAndSend("/topic/broadcast", new DefaultSTOMPMessage("SERVER", message, STOMPMessageType.ERROR, null, null));
    }
}
