package pm.transformer;

import message.completed.CompletedMoveMessage;
import message.created.JoinedPlayMoveMessage;
import message.created.PlayStateMessage;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class PlayTransformer implements Transformer<String, JoinedPlayMoveMessage, KeyValue<String, CompletedMoveMessage>> {
    private static final Logger logger = LoggerFactory.getLogger(PlayTransformer.class);

    private final String playStateStoreName;
    private KeyValueStore<String, PlayStateMessage> playStateKVStore;
    private ProcessorContext ctx;

    public PlayTransformer(String playStateStoreName) {
        this.playStateStoreName = playStateStoreName;
    }

    @Override
    public void init(ProcessorContext context) {
        this.playStateKVStore = (KeyValueStore<String, PlayStateMessage>) context.getStateStore(playStateStoreName);
        this.ctx = context;
    }

    @Override
    public KeyValue<String, CompletedMoveMessage> transform(String key, JoinedPlayMoveMessage value) {
        logger.info(key, value);
        String playID = value.getPlay().getID();
        PlayStateMessage curGame = playStateKVStore.get(playID);
        if (curGame == null) {
            playStateKVStore.put(playID, new PlayStateMessage(value));
        } else {
            boolean accepted = curGame.considerMove(value.getMove());
            if (!accepted) {
                return new KeyValue<>("ERROR", new CompletedMoveMessage());
            }
        }
        return null;
    }

    @Override
    public void close() {

    }
}
