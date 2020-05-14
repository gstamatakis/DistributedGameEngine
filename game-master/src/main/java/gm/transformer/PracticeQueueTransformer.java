package gm.transformer;

import message.created.PlayMessage;
import message.queue.PracticeQueueMessage;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

@SuppressWarnings("unchecked")
public class PracticeQueueTransformer implements Transformer<String, PracticeQueueMessage, KeyValue<String, PlayMessage>> {
    private static final Logger logger = LoggerFactory.getLogger(PracticeQueueTransformer.class);

    private final String pairPracticePlayersStore;
    private final String userToGameIDStore;
    private final String gameIDToGameStore;
    private KeyValueStore<String, PracticeQueueMessage> pairPracticePlayersKVStore;
    private KeyValueStore<String, PlayMessage> userToGameKVStore;
    private KeyValueStore<String, PlayMessage> gameIDToGameKVStore;
    private final long BATCH_DURATION_SEC = 3;

    public PracticeQueueTransformer(String pairPracticePlayersStore, String userToGameIDStore, String gameIDToGameStore) {
        this.pairPracticePlayersStore = pairPracticePlayersStore;
        this.userToGameIDStore = userToGameIDStore;
        this.gameIDToGameStore = gameIDToGameStore;
    }

    @Override
    public void init(ProcessorContext context) {
        this.pairPracticePlayersKVStore = (KeyValueStore<String, PracticeQueueMessage>) context.getStateStore(pairPracticePlayersStore);
        this.userToGameKVStore = (KeyValueStore<String, PlayMessage>) context.getStateStore(userToGameIDStore);
        this.gameIDToGameKVStore = (KeyValueStore<String, PlayMessage>) context.getStateStore(gameIDToGameStore);

        //Flush paired plays periodically
        context.schedule(Duration.ofSeconds(BATCH_DURATION_SEC), PunctuationType.WALL_CLOCK_TIME, timestamp -> {
            //Store iterator
            KeyValueIterator<String, PracticeQueueMessage> it = pairPracticePlayersKVStore.all();
            //Keep matching players as long as their are still available players
            while (it.hasNext()) {
                //Skip this execution if there are no available players
                KeyValue<String, PracticeQueueMessage> p1 = it.next();
                if (!it.hasNext()) {
                    return;
                }
                KeyValue<String, PracticeQueueMessage> p2 = it.next();

                //Get the messages and then delete them from the store
                PracticeQueueMessage msg1 = p1.value;
                PracticeQueueMessage msg2 = p2.value;
                pairPracticePlayersKVStore.delete(msg1.getCreatedBy());
                pairPracticePlayersKVStore.delete(msg2.getCreatedBy());

                //Create a new Play
                PlayMessage newPlay = new PlayMessage(msg1, msg2);
                userToGameKVStore.put(msg1.getCreatedBy(), newPlay);
                userToGameKVStore.put(msg2.getCreatedBy(), newPlay);
                gameIDToGameKVStore.put(newPlay.getID(), newPlay);

                //Send the new play to the destination topic
                context.forward(newPlay.getID(), newPlay);
                logger.info("Matching players: " + newPlay);
            }
        });
    }

    @Override
    public KeyValue<String, PlayMessage> transform(String principal, PracticeQueueMessage newJoinMsg) {
        pairPracticePlayersKVStore.put(principal, newJoinMsg);
        logger.debug("Enqueuing : " + principal + " | " + newJoinMsg.toString());
        return null;    //Null values are dropped from stream by default
    }

    @Override
    public void close() {
        if (pairPracticePlayersKVStore != null) {
            if (pairPracticePlayersKVStore.isOpen()) {
                pairPracticePlayersKVStore.close();
            }
        }
        if (userToGameKVStore != null) {
            if (userToGameKVStore.isOpen()) {
                userToGameKVStore.close();
            }
        }
        if (gameIDToGameKVStore != null) {
            if (gameIDToGameKVStore.isOpen()) {
                gameIDToGameKVStore.close();
            }
        }
    }

}