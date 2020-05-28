package ui.service;

import com.google.gson.Gson;
import message.DefaultKafkaMessage;
import message.completed.CompletedMoveMessage;
import message.completed.CompletedPlayMessage;
import message.completed.CompletedTournamentMessage;
import message.created.PlayMessage;
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
public class EventListenerService {
    private static final Logger logger = LoggerFactory.getLogger(EventListenerService.class);
    private final String newPlaysTopic = "new-plays";
    private final String ongoingPlaysTopic = "ongoing-plays";
    private final String completedPlaysTopic = "completed-plays";
    private final String completedMovesTopic = "completed-moves";
    private final String completedTournamentsTopic = "completed-tournaments";

    private final Gson gson = new Gson();
    private final long timeout = 5;

    @Autowired
    private KafkaTemplate<String, DefaultKafkaMessage> kafkaMessageTemplate;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private PlayRepository playRepository;


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

        //Spectators can now access this play
        messagingTemplate.convertAndSendToUser(newPlay.getP2(), "/queue/reply",
                new DefaultSTOMPMessage(
                        newPlay.getP2(),
                        newPlay.getP1(),
                        STOMPMessageType.GAME_START,
                        null,
                        playID));

        playRepository.saveAndFlush(new PlayEntity(newPlay));
    }


    @KafkaListener(topics = ongoingPlaysTopic, containerFactory = "kafkaDefaultListenerContainerFactory")
    public void listenOngoingPlays(@Payload String message,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
        if (message == null || message.equals("null")) {
            return;
        }

        //Convert the new play to an object
        PlayMessage ongoingPlay = (PlayMessage) gson.fromJson(message, DefaultKafkaMessage.class).retrieve(PlayMessage.class.getCanonicalName());
        String playID = ongoingPlay.getID();
        logger.info("listenOngoingPlays: Received ongoing play message: " + ongoingPlay.toString() + " from partition " + partition);

        //Alert the 2 users that they either need to move or wait for the opponent
        messagingTemplate.convertAndSendToUser(ongoingPlay.getNeedsToMove(), "/queue/reply",
                new DefaultSTOMPMessage(
                        ongoingPlay.getNeedsToMove(),
                        String.format("You need to make a move [%s] ", ongoingPlay.getNeedsToMove()),
                        STOMPMessageType.NEED_TO_MOVE,
                        null,
                        playID));

        messagingTemplate.convertAndSendToUser(ongoingPlay.getNeedsToWait(), "/queue/reply", new DefaultSTOMPMessage(
                ongoingPlay.getNeedsToWait(),
                String.format("Waiting for [%s] to make a move.", ongoingPlay.getNeedsToMove()),
                STOMPMessageType.NOTIFICATION,
                null,
                playID));
    }

    @KafkaListener(topics = completedTournamentsTopic, containerFactory = "kafkaDefaultListenerContainerFactory")
    public void listenForCompletedTournaments(@Payload String message,
                                              @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) throws InterruptedException, ExecutionException, TimeoutException {
        CompletedTournamentMessage completedTournamentMessage = (CompletedTournamentMessage) gson.fromJson(message, DefaultKafkaMessage.class).retrieve(CompletedTournamentMessage.class.getCanonicalName());
        logger.info("listenForCompletedTournaments: Received completed tournament message: " + completedTournamentMessage.toString() + " from partition " + partition);

        //Notification to all users
        String winnerUsernames = completedTournamentMessage.getWinnerUsernames().toString();
        for (String username : completedTournamentMessage.getWinnerUsernames()) {
            messagingTemplate.convertAndSendToUser(username, "/queue/reply",
                    new DefaultSTOMPMessage(
                            username,
                            String.format("Tournament [%s] winners [%s]", completedTournamentMessage.getId(), winnerUsernames),
                            STOMPMessageType.NOTIFICATION,
                            null,
                            completedTournamentMessage.getId()));
        }

        //Handle the spectators
        String playID = completedTournamentMessage.getId();
        String winner = completedTournamentMessage.getId();
        PlayEntity entry = playRepository.findByPlayID(playID);

        if (entry != null) {
            Set<String> spectators = entry.getSpectators();
            if (!spectators.isEmpty()) {
                for (String spectator : spectators) {
                    messagingTemplate.convertAndSendToUser(spectator, "/queue/reply",
                            new DefaultSTOMPMessage(spectator, winner, STOMPMessageType.GAME_OVER, null, playID));
                    logger.info(String.format("listenForCompletedPlays: Sent winner [%s] to spectator [%s] for playID=[%s].", winner, spectator, playID));
                }
            }
        }

        //Also save to database
        playRepository.saveAndFlush(new PlayEntity(completedTournamentMessage));
    }

    @KafkaListener(topics = completedPlaysTopic, containerFactory = "kafkaDefaultListenerContainerFactory")
    public void listenForCompletedPlays(@Payload String message,
                                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) throws InterruptedException, ExecutionException, TimeoutException {
        CompletedPlayMessage completedPlayMessage = (CompletedPlayMessage) gson.fromJson(message, DefaultKafkaMessage.class).retrieve(CompletedPlayMessage.class.getCanonicalName());
        logger.info("listenForCompletedPlays: Received completed play message: " + completedPlayMessage.toString() + " from partition " + partition);

        if (completedPlayMessage.isTie()) {
            messagingTemplate.convertAndSendToUser(completedPlayMessage.getP1(), "/queue/reply",
                    new DefaultSTOMPMessage(
                            completedPlayMessage.getP1(),
                            String.format("Game TIE between [%s] and [%s].", completedPlayMessage.getP1(), completedPlayMessage.getP2()),
                            STOMPMessageType.GAME_OVER,
                            null,
                            completedPlayMessage.getPlayID()));

            messagingTemplate.convertAndSendToUser(completedPlayMessage.getP2(), "/queue/reply",
                    new DefaultSTOMPMessage(
                            completedPlayMessage.getP2(),
                            String.format("Game TIE between [%s] and [%s].", completedPlayMessage.getP1(), completedPlayMessage.getP2()),
                            STOMPMessageType.GAME_OVER,
                            null,
                            completedPlayMessage.getPlayID()));
        } else {
            messagingTemplate.convertAndSendToUser(completedPlayMessage.getWinner(), "/queue/reply",
                    new DefaultSTOMPMessage(
                            completedPlayMessage.getWinner(),
                            String.format("Winner=[%s],Loser=[%s]", completedPlayMessage.getWinner(), completedPlayMessage.getLoser()),
                            STOMPMessageType.GAME_OVER,
                            null,
                            completedPlayMessage.getPlayID()));

            messagingTemplate.convertAndSendToUser(completedPlayMessage.getLoser(), "/queue/reply",
                    new DefaultSTOMPMessage(
                            completedPlayMessage.getLoser(),
                            String.format("Winner=[%s],Loser=[%s]", completedPlayMessage.getWinner(), completedPlayMessage.getLoser()),
                            STOMPMessageType.GAME_OVER,
                            null,
                            completedPlayMessage.getPlayID()));
        }

        //Handle the spectators
        String playID = completedPlayMessage.getPlayID();
        String winner = completedPlayMessage.getWinner();
        PlayEntity entry = playRepository.findByPlayID(playID);

        if (entry != null) {
            Set<String> spectators = entry.getSpectators();
            if (!spectators.isEmpty()) {
                for (String spectator : spectators) {
                    messagingTemplate.convertAndSendToUser(spectator, "/queue/reply",
                            new DefaultSTOMPMessage(spectator, winner, STOMPMessageType.GAME_OVER, null, playID));
                    logger.info(String.format("listenForCompletedPlays: Sent winner [%s] to spectator [%s] for playID=[%s].", winner, spectator, playID));
                }
            }
        }

        //Send a Tombstone message to the ongoing-plays topic to remove the finished play
        //Make sure necessary info like scores is kept safe
        kafkaMessageTemplate
                .send(ongoingPlaysTopic, "key", new DefaultKafkaMessage())    //TOMBSTONE MESSAGE
                .get(timeout, TimeUnit.SECONDS);

        //Also save to database
        playRepository.saveAndFlush(new PlayEntity(completedPlayMessage));
    }

    @KafkaListener(topics = completedMovesTopic, containerFactory = "kafkaDefaultListenerContainerFactory")
    public void listenForCompletedMoves(@Payload String message,
                                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
        CompletedMoveMessage completedMoveMessage = (CompletedMoveMessage) gson.fromJson(message, DefaultKafkaMessage.class).retrieve(CompletedMoveMessage.class.getCanonicalName());
        String playPayload = gson.toJson(completedMoveMessage);
        logger.info("listenForCompletedMoves: Received completed move message: " + completedMoveMessage.toString() + " from partition " + partition);

        if (completedMoveMessage.isValid()) {
            messagingTemplate.convertAndSendToUser(completedMoveMessage.getPlayedByUsername(), "/queue/reply",
                    new DefaultSTOMPMessage(
                            completedMoveMessage.getPlayedByUsername(),
                            playPayload,
                            STOMPMessageType.MOVE_ACCEPTED,
                            null,
                            completedMoveMessage.getMoveMessage().getPlayID()));
            messagingTemplate.convertAndSendToUser(completedMoveMessage.getOpponentUsername(), "/queue/reply",
                    new DefaultSTOMPMessage(
                            completedMoveMessage.getPlayedByUsername(),
                            playPayload,
                            STOMPMessageType.MOVE_ACCEPTED,
                            null,
                            completedMoveMessage.getMoveMessage().getPlayID()));

            //Handle the spectators
            String playID = completedMoveMessage.getMoveMessage().getPlayID();
            PlayEntity entry = playRepository.findByPlayID(playID);

            if (entry != null) {
                Set<String> spectators = entry.getSpectators();
                if (!spectators.isEmpty()) {
                    for (String spectator : spectators) {
                        messagingTemplate.convertAndSendToUser(spectator, "/queue/reply",
                                new DefaultSTOMPMessage(spectator, playPayload, STOMPMessageType.MOVE_ACCEPTED, null, playID));
                        logger.info(String.format("listenForCompletedMoves: Sent play [%s] to spectator [%s] for playID=[%s].", playPayload, spectator, playID));
                    }
                }
            }

        } else {
            if (completedMoveMessage.getPlayedByUsername() == null) {
                logger.error(String.format("listenForCompletedMoves: Invalid move message can't be sent back due to null username [%s]", completedMoveMessage.toString()));
                return;
            }
            messagingTemplate.convertAndSendToUser(completedMoveMessage.getPlayedByUsername(), "/queue/reply",
                    new DefaultSTOMPMessage(
                            completedMoveMessage.getPlayedByUsername(),
                            playPayload,
                            STOMPMessageType.MOVE_DENIED,
                            null,
                            completedMoveMessage.getMoveMessage().getPlayID()));

        }
    }
}
