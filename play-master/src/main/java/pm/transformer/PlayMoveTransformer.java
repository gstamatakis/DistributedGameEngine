package pm.transformer;

import game.AbstractGameState;
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

public class PlayMoveTransformer implements Transformer<String, JoinedPlayMoveMessage, KeyValue<String, DefaultKafkaMessage>> {
    private static final Logger logger = LoggerFactory.getLogger(PlayMoveTransformer.class);
    private ProcessorContext ctx;

    @Override
    public void init(ProcessorContext context) {
        this.ctx = context;
    }

    @Override
    public KeyValue<String, DefaultKafkaMessage> transform(String key, JoinedPlayMoveMessage value) {
        logger.info(String.format("transform() received: %s", value.toString()));

        //Extract the 2 joined objects
        MoveMessage move = value.getMove();
        PlayMessage play = value.getPlay();

        //Retrieve state, modify state, save state
        AbstractGameState curGameState = play.getGameState();
        CompletedMoveMessage output_move = curGameState.offerMove(move);
        play.setGameState(curGameState);
        if (output_move.isValid()) {
            play.setLastUserWhoMoved(move.getUsername());  //
        }

        //Forward the new move
        logger.info(String.format("transform() forwarding output move [%s].", output_move.toString()));
        this.ctx.forward(key, new DefaultKafkaMessage(output_move, CompletedMoveMessage.class.getCanonicalName()));

        //If play has finished send a message declaring the end of the play
        if (curGameState.isFinished()) {
            CompletedPlayMessage completedPlayMessage = new CompletedPlayMessage(
                    play.getID(), curGameState.getPlaysFirstUsername(), curGameState.getPlaysSecondUsername(),
                    curGameState.getWinner(), curGameState.getCreatedBy(), play.getGameTypeEnum(), play.getPlayTypeEnum());

            //Send a completed play message
            logger.info(String.format("transform() forwarding completed play [%s].", completedPlayMessage.toString()));
            this.ctx.forward(key, new DefaultKafkaMessage(completedPlayMessage, CompletedPlayMessage.class.getCanonicalName()));
        } else {
            logger.info(String.format("transform() forwarding ongoing play [%s].", play.toString()));
            this.ctx.forward(key, new DefaultKafkaMessage(play, PlayMessage.class.getCanonicalName()));
        }

        //Return nothing by default
        return null;
    }

    @Override
    public void close() {

    }
}
