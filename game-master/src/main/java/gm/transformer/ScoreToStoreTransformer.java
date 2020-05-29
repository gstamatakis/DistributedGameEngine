package gm.transformer;

import message.DefaultKafkaMessage;
import message.completed.UserScore;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class ScoreToStoreTransformer implements Transformer<String, DefaultKafkaMessage, KeyValue<String, String>> {
    private static final Logger logger = LoggerFactory.getLogger(ScoreToStoreTransformer.class);

    private final String storeName;
    private KeyValueStore<String, UserScore> kvStore;

    public ScoreToStoreTransformer(String storeName) {
        this.storeName = storeName;
    }

    @Override
    public void init(ProcessorContext context) {
        this.kvStore = (KeyValueStore<String, UserScore>) context.getStateStore(storeName);
    }

    @Override
    public KeyValue<String, String> transform(String key, DefaultKafkaMessage value) {
        UserScore userScore = (UserScore) value.retrieve(UserScore.class.getCanonicalName());
        UserScore retrieved = kvStore.get(key);
        if (retrieved == null) {
            kvStore.put(key, userScore);
            logger.info(String.format("Inserted [%s],[%s].", key, userScore.toString()));
            return null;
        }
        //Merge the 2 user's scores and update the key
        UserScore merged = userScore.merge(retrieved);
        kvStore.put(key, merged);
        logger.info(String.format("Updated [%s],[%s].", key, userScore.toString()));
        return null;
    }

    @Override
    public void close() {

    }
}
