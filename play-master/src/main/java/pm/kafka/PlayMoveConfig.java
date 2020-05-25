package pm.kafka;

import message.DefaultKafkaMessage;
import message.completed.CompletedMoveMessage;
import message.completed.CompletedPlayMessage;
import message.created.JoinedPlayMoveMessage;
import message.created.MoveMessage;
import message.created.PlayMessage;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.ValueJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import pm.transformer.PlayMoveTransformer;

import java.util.function.BiFunction;

@SuppressWarnings("unchecked")
@Component
public class PlayMoveConfig {
    private static final Logger logger = LoggerFactory.getLogger(PlayMoveConfig.class);

    @Bean
    public BiFunction<KStream<String, DefaultKafkaMessage>, KTable<String, DefaultKafkaMessage>, KStream<String, DefaultKafkaMessage>[]> processPlaysAndMoves() {
        return (moveStream, playTable) -> {
            //Log incoming stream
            moveStream.foreach((k, v) -> {
                logger.info(String.format("processPlaysAndMoves: Consumed move [%s],[%s]", k, v != null ? v.toString() : null));
            });

            //Inner-Join the player moves with the available plays
            KStream<String, DefaultKafkaMessage> processedMoves = moveStream
                    .leftJoin(playTable, new PlayMoveJoiner())
                    .filter((key, value) -> value != null)
                    .transform(PlayMoveTransformer::new);

            //Send to output topics separately
            KStream<String, DefaultKafkaMessage>[] branches = processedMoves.branch(
                    (id, msg) -> msg.isType(PlayMessage.class.getCanonicalName()),  //Ongoing plays -> Back to ongoing-plays
                    (id, msg) -> msg.isType(CompletedMoveMessage.class.getCanonicalName()), //Finished plays -> Send to completed-moves
                    (id, msg) -> msg.isType(CompletedPlayMessage.class.getCanonicalName()));//Processed moves -> Send to completed-plays

            //Log results
            branches[0].foreach((k, v) -> logger.info(String.format("processPlaysAndMoves: Producing ongoing-play [%s],[%s].", k, v == null ? null : v.toString())));
            branches[1].foreach((k, v) -> logger.info(String.format("processPlaysAndMoves: Producing completed-move [%s],[%s].", k, v == null ? null : v.toString())));
            branches[2].foreach((k, v) -> logger.info(String.format("processPlaysAndMoves: Producing completed-plays [%s],[%s].", k, v == null ? null : v.toString())));

            return branches;
        };
    }

    //Stream joiner of Plays and Moves
    private static class PlayMoveJoiner implements ValueJoiner<DefaultKafkaMessage, DefaultKafkaMessage, JoinedPlayMoveMessage> {

        @Override
        public JoinedPlayMoveMessage apply(DefaultKafkaMessage moveMsg, DefaultKafkaMessage playMsg) {
            if (moveMsg == null && playMsg == null) {
                logger.error("PlayMoveJoiner: Received null play message and null move message!");
                return null;
            }
            if (playMsg == null) {
                logger.error(String.format("PlayMoveJoiner: Received null play message for move message [%s]", moveMsg.toString()));
                return null;
            }
            if (moveMsg == null) {
                logger.error(String.format("PlayMoveJoiner: Received null move message for play message [%s]", playMsg.toString()));
                return null;
            }
            MoveMessage move = (MoveMessage) moveMsg.retrieve(MoveMessage.class.getCanonicalName());
            PlayMessage play = (PlayMessage) playMsg.retrieve(PlayMessage.class.getCanonicalName());
            if (move == null || play == null) {
                logger.error(String.format("PlayMoveJoiner.apply error joining move with plays [%s][%s].",
                        move == null ? null : move.toString(),
                        play == null ? null : play.toString()));
                return null;
            }
            return new JoinedPlayMoveMessage(move, play);
        }
    }
}
