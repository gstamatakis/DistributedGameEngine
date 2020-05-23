package pm.transformer;

import message.created.PlayMessage;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;

@SuppressWarnings("unchecked")
public class PlayTransformer implements Transformer<String, PlayMessage, KeyValue<String, PlayMessage>> {
    private final String playStateStoreName;
    private KeyValueStore<String, PlayMessage> playsKVStore;
    private ProcessorContext ctx;
    private int testCnt;

    public PlayTransformer(String playStateStoreName) {
        this.playStateStoreName = playStateStoreName;
        this.testCnt = 0;
    }

    @Override
    public void init(ProcessorContext context) {
        this.playsKVStore = (KeyValueStore<String, PlayMessage>) context.getStateStore(playStateStoreName);
        this.ctx = context;
        this.ctx.schedule(Duration.ofSeconds(10), PunctuationType.WALL_CLOCK_TIME, timestamp -> {
            if (this.testCnt > 0) {
                this.playsKVStore.put("TEST", new PlayMessage());   //FIXME For testing purposes
            }
            this.testCnt++;
        });
    }

    @Override
    public KeyValue<String, PlayMessage> transform(String key, PlayMessage value) {
        //Delete incoming messages with null values since they signify a completed play
        if (value == null) {
            playsKVStore.delete(key);   //Tombstone message
        } else {
            playsKVStore.put(key, value);
        }
        return null;
    }

    @Override
    public void close() {

    }
}
