package ui.service;

import message.DefaultKafkaMessage;
import message.created.MoveMessage;
import message.queue.CreateTournamentQueueMessage;
import message.queue.JoinTournamentQueueMessage;
import message.queue.PracticeQueueMessage;
import message.requests.RequestCreateTournamentMessage;
import message.requests.RequestPracticeMessage;
import model.GameTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
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
    private final String newMovesTopic = "new-moves";
    private final long timeout = 5;

    @Autowired
    private KafkaTemplate<String, DefaultKafkaMessage> kafkaMessageTemplate;

    @Autowired
    private PlayRepository playRepository;

    public void enqueuePractice(String username, RequestPracticeMessage reqMsg) throws InterruptedException, ExecutionException, TimeoutException {
        PracticeQueueMessage newMsg = new PracticeQueueMessage(username, reqMsg);
        kafkaMessageTemplate
                .send(joinPlayTopic, username, new DefaultKafkaMessage(newMsg, PracticeQueueMessage.class.getCanonicalName()))
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
        logger.info(String.format("Enqueuing practice join [%s]", message));

        //Send the message
        kafkaMessageTemplate
                .send(joinPlayTopic, tournamentID, new DefaultKafkaMessage(message, CreateTournamentQueueMessage.class.getCanonicalName()))
                .get(timeout, TimeUnit.SECONDS);
    }

    //Queue a user for a tournament
    public void joinTournament(String username, String tournamentID) throws InterruptedException, ExecutionException, TimeoutException {
        JoinTournamentQueueMessage newMsg = new JoinTournamentQueueMessage(username, tournamentID);
        logger.info(String.format("Enqueuing tournament join [%s]", newMsg));
        kafkaMessageTemplate
                .send(joinPlayTopic, tournamentID, new DefaultKafkaMessage(newMsg, JoinTournamentQueueMessage.class.getCanonicalName()))
                .get(timeout, TimeUnit.SECONDS);
    }

    //Send new move to play
    public void sendMoveToPlay(String sentBy, String move, String playID) throws InterruptedException, ExecutionException, TimeoutException {
        MoveMessage newMsg = new MoveMessage(sentBy, move, playID);
        logger.info(String.format("Enqueuing move [%s]", newMsg));
        kafkaMessageTemplate
                .send(newMovesTopic, playID, new DefaultKafkaMessage(newMsg, MoveMessage.class.getCanonicalName()))
                .get(timeout, TimeUnit.SECONDS);
    }
}
