package ui.service;

import com.google.gson.Gson;
import model.GameTypeEnum;
import message.DefaultKafkaMessage;
import message.completed.CompletedMoveMessage;
import message.completed.CompletedPlayMessage;
import message.created.PlayMessage;
import message.queue.JoinTournamentQueueMessage;
import message.requests.RequestCreateTournamentMessage;
import message.queue.CreateTournamentQueueMessage;
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
import ui.model.PlayEntity;
import ui.repository.PlayRepository;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class PlayService {
    private static final Logger logger = LoggerFactory.getLogger(PlayService.class);
    private final String joinPlayTopic = "join-play";
    private final String newPlaysTopic = "new-plays";
    private final String completedPlaysTopic = "completed-plays";
    private final String outMovesTopic = "out-moves";
    private final String errorsTopic = "errors";
    private final Gson gson = new Gson();
    private final long timeout = 5;

    @Autowired
    private KafkaTemplate<String, DefaultKafkaMessage> kafkaJoinQueueTemplate;

    @Autowired
    private PlayRepository playRepository;

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

    @KafkaListener(topics = newPlaysTopic, containerFactory = "kafkaDefaultListenerContainerFactory")
    public void listenForNewPlays(@Payload String message,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                  @Header(KafkaHeaders.OFFSET) int offset) {
        PlayMessage newPlay = gson.fromJson(message, PlayMessage.class);
        logger.info("Received Message: " + newPlay.toString() + " from partition " + partition);
    }

    @KafkaListener(topics = completedPlaysTopic, containerFactory = "kafkaDefaultListenerContainerFactory")
    public void listenForFinishedPlays(@Payload String message,
                                       @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                       @Header(KafkaHeaders.OFFSET) int offset) {
        CompletedPlayMessage completedPlayMessage = gson.fromJson(message, CompletedPlayMessage.class);
        playRepository.save(new PlayEntity(completedPlayMessage));
        logger.info("Received Message: " + completedPlayMessage.toString() + " from partition " + partition);
    }

    @KafkaListener(topics = outMovesTopic, containerFactory = "kafkaDefaultListenerContainerFactory")
    public void listenForFinishedMoves(@Payload String message,
                                       @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
                                       @Header(KafkaHeaders.OFFSET) int offset) {
        CompletedMoveMessage completedMoveMessage = gson.fromJson(message, CompletedMoveMessage.class);
        logger.info("Received Message: " + completedMoveMessage.toString() + " from partition " + partition);
    }

    @KafkaListener(topics = errorsTopic, containerFactory = "kafkaListenerContainerFactory")
    public void listenForErrors(@Payload String message) {
        logger.info("Received Message: " + message);
    }
}
