package pm.transformer;

import message.completed.CompletedMoveMessage;
import message.created.JoinedPlayMoveMessage;
import message.created.MoveMessage;
import message.created.PlayMessage;
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
    private KeyValueStore<String, PlayMessage> playStateKVStore;
    private ProcessorContext ctx;

    public PlayTransformer(String playStateStoreName) {
        this.playStateStoreName = playStateStoreName;
    }

    @Override
    public void init(ProcessorContext context) {
        this.playStateKVStore = (KeyValueStore<String, PlayMessage>) context.getStateStore(playStateStoreName);
        this.ctx = context;
    }

    @Override
    public KeyValue<String, CompletedMoveMessage> transform(String key, JoinedPlayMoveMessage value) {
        logger.info(key, value);
        String playID = value.getPlay().getID();
        MoveMessage input_move = value.getMove();
        PlayMessage input_play = value.getPlay();

        PlayMessage curGame = playStateKVStore.get(playID);
        if (curGame == null) {
            curGame = input_play;
        }

        CompletedMoveMessage output_move = curGame.getGameType().offerMove(input_move);
        playStateKVStore.put(playID, curGame);
        return new KeyValue<>(key, output_move);
    }

    @Override
    public void close() {

    }
}
