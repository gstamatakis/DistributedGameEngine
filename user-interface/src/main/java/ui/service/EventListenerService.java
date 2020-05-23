package ui.service;

import com.google.gson.Gson;
import message.DefaultKafkaMessage;
import message.completed.CompletedMoveMessage;
import message.completed.CompletedPlayMessage;
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

        messagingTemplate.convertAndSendToUser(completedPlayMessage.getWinnerPlayer(), "/queue/reply",
                new DefaultSTOMPMessage(
                        completedPlayMessage.getWinnerPlayer(),
                        String.format("You *WON* against %s", completedPlayMessage.getLoserPlayer()),
                        STOMPMessageType.GAME_OVER,
                        null,
                        completedPlayMessage.getPlayID()));

        messagingTemplate.convertAndSendToUser(completedPlayMessage.getLoserPlayer(), "/queue/reply",
                new DefaultSTOMPMessage(
                        completedPlayMessage.getLoserPlayer(),
                        String.format("You *LOST* against %s", completedPlayMessage.getWinnerPlayer()),
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
