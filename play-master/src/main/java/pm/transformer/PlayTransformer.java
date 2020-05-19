package pm.transformer;

import game.ChessGameImpl;
import game.AbstractGameType;
import game.TicTacToeGameImpl;
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

        PlayMessage curPlayMessage = playStateKVStore.get(playID);
        if (curPlayMessage == null) {
            curPlayMessage = input_play;
        }

        AbstractGameType curAbstractGameType = curPlayMessage.getAbstractGameType();
        CompletedMoveMessage output_move;

        switch (curAbstractGameType.getGameTypeEnum()) {
            case TIC_TAC_TOE:
                TicTacToeGameImpl specificTTT = (TicTacToeGameImpl) curAbstractGameType;
                output_move = specificTTT.offerMove(input_move);
                break;
            case CHESS:
                ChessGameImpl specificChess = (ChessGameImpl) curAbstractGameType;
                output_move = specificChess.offerMove(input_move);
                break;
            default:
                throw new IllegalStateException("Default case in PlayTransformer.transform()");
        }

        playStateKVStore.put(playID, curPlayMessage);
        return new KeyValue<>(key, output_move);
    }

    @Override
    public void close() {

    }
}
