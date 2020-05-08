package gm.kafka;

import message.JoinPlayMessage;
import message.PlayMessage;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class PracticeQueueTransformer implements Transformer<String, JoinPlayMessage, KeyValue<String, PlayMessage>> {
    private final String pairPracticePlayersStore;
    private final String userToGameIDStore;
    private final String gameIDToGameStore;
    private KeyValueStore<String, JoinPlayMessage> pairPracticePlayersKVStore;
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
        this.pairPracticePlayersKVStore = (KeyValueStore<String, JoinPlayMessage>) context.getStateStore(pairPracticePlayersStore);
        this.userToGameKVStore = (KeyValueStore<String, PlayMessage>) context.getStateStore(userToGameIDStore);
        this.gameIDToGameKVStore = (KeyValueStore<String, PlayMessage>) context.getStateStore(gameIDToGameStore);

        //Flush paired plays periodically
        context.schedule(Duration.ofSeconds(BATCH_DURATION_SEC), PunctuationType.WALL_CLOCK_TIME, timestamp -> {
            try (KeyValueIterator<String, JoinPlayMessage> it = pairPracticePlayersKVStore.all()) {
                //Skip this execution if there are no available players
                if (!it.hasNext()) {
                    return;
                }

                //Load everything to a list
                List<JoinPlayMessage> list = new ArrayList<>();
                it.forEachRemaining(item -> list.add(item.value));

                //Keep the players an even number
                if (list.size() % 2 == 1) {
                    list.remove(0);
                }

                //Pair the players
                for (int i = 0; i < list.size(); i += 2) {
                    //Get the messages and then delete them from the store
                    JoinPlayMessage msg1 = list.get(i);
                    JoinPlayMessage msg2 = list.get(i + 1);
                    pairPracticePlayersKVStore.delete(msg1.getUsername());
                    pairPracticePlayersKVStore.delete(msg2.getUsername());

                    //Create a new Play
                    PlayMessage newPlay = new PlayMessage(msg1, msg2);
                    userToGameKVStore.put(msg1.getUsername(), newPlay);
                    userToGameKVStore.put(msg2.getUsername(), newPlay);
                    gameIDToGameKVStore.put(newPlay.getPlayID(), newPlay);

                    //Send the new play to the destination topic
                    context.forward(newPlay.getPlayID(), newPlay);
                    System.out.println("Forwarding: " + newPlay);
                }
            }
        });
    }

    @Override
    public KeyValue<String, PlayMessage> transform(String principal, JoinPlayMessage newJoinMsg) {
        pairPracticePlayersKVStore.put(principal, newJoinMsg);
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