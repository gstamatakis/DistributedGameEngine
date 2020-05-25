package gm.transformer;

import message.DefaultKafkaMessage;
import message.completed.UserScore;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

@SuppressWarnings("unchecked")
public class ScoreToStoreTransformer implements Transformer<String, DefaultKafkaMessage, KeyValue<String, String>> {
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
        this.kvStore.put(key, userScore);
        return null;
    }

    @Override
    public void close() {

    }
}
