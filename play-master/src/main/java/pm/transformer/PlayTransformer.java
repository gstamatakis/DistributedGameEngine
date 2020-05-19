package pm.transformer;

import game.AbstractGameType;
import message.DefaultKafkaMessage;
import message.completed.CompletedMoveMessage;
import message.completed.CompletedPlayMessage;
import message.created.JoinedPlayMoveMessage;
import message.created.MoveMessage;
import message.created.PlayMessage;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayTransformer implements Transformer<String, JoinedPlayMoveMessage, KeyValue<String, DefaultKafkaMessage>> {
    private static final Logger logger = LoggerFactory.getLogger(PlayTransformer.class);

    private ProcessorContext ctx;

    public PlayTransformer() {
    }

    @Override
    public void init(ProcessorContext context) {
        this.ctx = context;
    }

    @Override
    public KeyValue<String, DefaultKafkaMessage> transform(String key, JoinedPlayMoveMessage value) {
        logger.info(key, value);

        MoveMessage input_move = value.getMove();
        PlayMessage input_play = value.getPlay();

        AbstractGameType curGame = input_play.getAbstractGameType();
        CompletedMoveMessage output_move = curGame.offerMove(input_move);

        //Forward the new move
        this.ctx.forward(key, new DefaultKafkaMessage(output_move));

        //If play has finished send a message declaring the end of the play
        if (curGame.getWinner() != null) {
            CompletedPlayMessage completedPlayMessage = new CompletedPlayMessage(
                    input_play.getID(), curGame.getWinner(), curGame.getLoser(),
                    curGame.getCreatedBy(), input_play.getGameTypeEnum(), input_play.getPlayTypeEnum());

            this.ctx.forward(key, new DefaultKafkaMessage(completedPlayMessage));
        }

        //Return nothing
        return null;
    }

    @Override
    public void close() {

    }
}
