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
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class PlayTransformer implements Transformer<String, JoinedPlayMoveMessage, KeyValue<String, DefaultKafkaMessage>> {
    private static final Logger logger = LoggerFactory.getLogger(PlayTransformer.class);

    private ProcessorContext ctx;
    private String playStoreName;
    private String completedPlayStoreName;
    private KeyValueStore<String, PlayMessage> playStateKVStore;
    private KeyValueStore<String, String> completedPlayStoreKVStore;

    public PlayTransformer(String playStateStoreName, String completedPlayStoreName) {
        this.playStoreName = playStateStoreName;
        this.completedPlayStoreName = completedPlayStoreName;
    }

    @Override
    public void init(ProcessorContext context) {
        this.ctx = context;
        this.playStateKVStore = (KeyValueStore<String, PlayMessage>) context.getStateStore(playStoreName);
        this.completedPlayStoreKVStore = (KeyValueStore<String, String>) context.getStateStore(completedPlayStoreName);
    }

    @Override
    public KeyValue<String, DefaultKafkaMessage> transform(String key, JoinedPlayMoveMessage value) {
        logger.info(String.format("PlayTransformer received: %s", value.toString()));

        //Extract the 2 joined objects
        MoveMessage move = value.getMove();
        PlayMessage play = value.getPlay();
        final String playID = play.getID();

        //Check if this message is for a play that has already finished
        String completedPlayWinner = completedPlayStoreKVStore.get(playID);
        if (completedPlayWinner != null) {
            logger.error(String.format("Received message for completed play [%s], won by [%s].", key, completedPlayWinner));
            return null;
        }

        //Check if we have encountered a play with the same ID before
        PlayMessage storeLookupResult = this.playStateKVStore.get(playID);
        if (storeLookupResult != null) {
            play = storeLookupResult;
        }

        //Retrieve state, modify state, save state
        AbstractGameState curGameState = play.getGameState();
        CompletedMoveMessage output_move = curGameState.offerMove(move);
        play.setGameState(curGameState);

        //Forward the new move
        this.ctx.forward(key, new DefaultKafkaMessage(output_move, CompletedMoveMessage.class.getCanonicalName()));

        //If play has finished send a message declaring the end of the play
        if (curGameState.getWinner() != null) {
            CompletedPlayMessage completedPlayMessage = new CompletedPlayMessage(
                    play.getID(), curGameState.getWinner(), curGameState.getLoser(),
                    curGameState.getCreatedBy(), play.getGameTypeEnum(), play.getPlayTypeEnum()
            );

            //Send a completed play message
            this.ctx.forward(key, new DefaultKafkaMessage(completedPlayMessage, CompletedPlayMessage.class.getCanonicalName()));
            this.completedPlayStoreKVStore.put(playID, curGameState.getWinner());
        } else {

            //Update the state of the play
            this.playStateKVStore.put(playID, play);
        }

        //Return nothing by default
        return null;
    }

    @Override
    public void close() {

    }
}
