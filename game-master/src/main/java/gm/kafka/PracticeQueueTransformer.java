package gm.kafka;

import message.PracticePlayMessage;
import message.UserJoinQueueMessage;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

@SuppressWarnings("unchecked")
public class PracticeQueueTransformer implements Transformer<String, UserJoinQueueMessage, KeyValue<String, PracticePlayMessage>> {
    private final String stateStoreName;
    private ProcessorContext context;
    private KeyValueStore<String, UserJoinQueueMessage> kvStore;

    public PracticeQueueTransformer(String stateStoreName) {
        this.stateStoreName = stateStoreName;
    }

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
        this.kvStore = (KeyValueStore<String, UserJoinQueueMessage>) context.getStateStore(stateStoreName);
    }

    @Override
    public KeyValue<String, PracticePlayMessage> transform(String key, UserJoinQueueMessage value) {
        try (KeyValueIterator<String, UserJoinQueueMessage> it = kvStore.all()) {
            if (it.hasNext()) {
                KeyValue<String, UserJoinQueueMessage> item = it.next();
                kvStore.delete(item.key);
                PracticePlayMessage output = new PracticePlayMessage(item.value, value);
                context.forward(output.getPlayID(), output);
            } else {
                kvStore.put(key, value);
            }
        }
        return null;
    }

    @Override
    public void close() {
        if (kvStore != null) {
            kvStore.close();
        }
    }

}