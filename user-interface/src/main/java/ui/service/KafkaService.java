package ui.service;

import com.google.gson.Gson;
import game.GameType;
import message.DefaultKafkaMessage;
import message.DefaultPlayMessage;
import message.PlayTypeMessage;
import message.created.PlayMessage;
import message.queue.TournamentQueueMessage;
import message.requests.RequestCreateTournamentMessage;
import message.requests.RequestJoinTournamentMessage;
import message.requests.RequestPracticeMessage;
import message.queue.PracticeQueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import websocket.MyStompSessionHandler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class KafkaService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);
    private final String joinPlayTopic = "join-play";
    private final String playsTopic = "plays";
    private final String errorsTopic = "errors";
    private final Gson gson = new Gson();
    private final long timeout = 5;
    private final MessageDigest messageDigest;

    @Autowired
    private KafkaTemplate<String, DefaultKafkaMessage> kafkaJoinQueueTemplate;

    public KafkaService() throws NoSuchAlgorithmException {
        this.messageDigest = MessageDigest.getInstance("SHA-256");
    }

    public void enqueuePractice(String username, RequestPracticeMessage reqMsg) throws InterruptedException, ExecutionException, TimeoutException {
        kafkaJoinQueueTemplate
                .send(joinPlayTopic, username, new DefaultKafkaMessage(new PracticeQueueMessage(username, reqMsg)))
                .get(timeout, TimeUnit.SECONDS);
    }

    //Create a tournament ID
    //Use the tournament ID as the key
    //TODO consider verifying uniqueness of tournament ID.
    public String createTournament(String username, RequestCreateTournamentMessage reqMsg) throws InterruptedException, ExecutionException, TimeoutException {
        //Create the tournamentID by hashing part of the input
        messageDigest.reset();
        messageDigest.update(String.format("%s_%s_%d", username, reqMsg.getGameType().toString(), System.nanoTime()).getBytes());
        String tournamentID = new String(messageDigest.digest());

        //Send the message
        kafkaJoinQueueTemplate
                .send(joinPlayTopic, tournamentID, new DefaultKafkaMessage(new RequestCreateTournamentMessage(username, reqMsg, tournamentID)))
                .get(timeout, TimeUnit.SECONDS);

        return tournamentID;
    }

    //Queue a user for a tournament
    public void enqueueTournament(String username, RequestJoinTournamentMessage joinMsg) throws InterruptedException, ExecutionException, TimeoutException {
        kafkaJoinQueueTemplate
                .send(joinPlayTopic, username, new DefaultKafkaMessage(new TournamentQueueMessage(username, joinMsg)))
                .get(timeout, TimeUnit.SECONDS);
    }

    @KafkaListener(topics = playsTopic, containerFactory = "kafkaDefaultListenerContainerFactory")
    public void listenForNewPlays(@Payload String message,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                  @Header(KafkaHeaders.OFFSET) int offset) {
        PlayMessage newPlay = gson.fromJson(message, PlayMessage.class);
        logger.info("Received Message: " + newPlay.toString() + "from partition: " + partition);
    }

    @KafkaListener(topics = errorsTopic, containerFactory = "kafkaListenerContainerFactory")
    public void listenForErrors(@Payload String message) {
        logger.info("Received Message: " + message);
    }
}
