package pm.transformer;

import message.created.PlayMessage;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class PlayTransformer implements Transformer<String, PlayMessage, KeyValue<String, PlayMessage>> {
    private static final Logger logger = LoggerFactory.getLogger(PlayTransformer.class);

    private final String playStateStoreName;
    private KeyValueStore<String, PlayMessage> playsKVStore;

    public PlayTransformer(String playStateStoreName) {
        this.playStateStoreName = playStateStoreName;
    }

    @Override
    public void init(ProcessorContext context) {
        this.playsKVStore = (KeyValueStore<String, PlayMessage>) context.getStateStore(playStateStoreName);
    }

    @Override
    public KeyValue<String, PlayMessage> transform(String key, PlayMessage value) {
        //Delete incoming messages with null values since they signify a completed play
        if (value == null) {
            playsKVStore.delete(key);
            logger.info(String.format("transform() deleted key [%s]", key));
        } else {
            playsKVStore.put(key, value);
            logger.info(String.format("transform() updated [%s,%s]", key, value.toString()));
        }
        return null;
    }

    @Override
    public void close() {

    }
}
