package pm.transformer;

import message.created.PlayMessage;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public class PlayTransformer implements Transformer<String, PlayMessage, KeyValue<String, PlayMessage>> {
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
        } else {
            playsKVStore.put(key, value);
        }
        return null;
    }

    @Override
    public void close() {

    }
}
