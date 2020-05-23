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

@Component
public class PlayMoveConfig {
    private static final Logger logger = LoggerFactory.getLogger(PlayMoveConfig.class);

    @Bean
    public BiFunction<KStream<String, DefaultKafkaMessage>, KTable<String, DefaultKafkaMessage>, KStream<String, DefaultKafkaMessage>[]> processPlaysAndMoves() {
        return (moveStream, playTable) -> {
            //Inner-Join the player moves with the available plays
            KStream<String, DefaultKafkaMessage> processedMoves = moveStream
                    .leftJoin(playTable, new PlayMoveJoiner())
                    .filter((key, value) -> value != null)
                    .transform(PlayMoveTransformer::new);

            //Send to output topics separately
            return processedMoves.branch(
                    (id, msg) -> msg.isType(PlayMessage.class.getCanonicalName()),  //On-going plays -> Back to onGOING-plays
                    (id, msg) -> msg.isType(CompletedMoveMessage.class.getCanonicalName()), //Finished plays -> Send to completed-plays
                    (id, msg) -> msg.isType(CompletedPlayMessage.class.getCanonicalName()));//Processed moves -> Send to completed-moves
        };
    }

    //Stream joiner of Plays and Moves
    private static class PlayMoveJoiner implements ValueJoiner<DefaultKafkaMessage, DefaultKafkaMessage, JoinedPlayMoveMessage> {

        @Override
        public JoinedPlayMoveMessage apply(DefaultKafkaMessage moveMsg, DefaultKafkaMessage playMsg) {
            if (moveMsg == null && playMsg == null) {
                logger.error("Received null play message and null move message!");
                return null;
            }
            if (playMsg == null) {
                logger.error(String.format("Received null play message for move message [%s]", moveMsg.toString()));
                return null;
            }
            if (moveMsg == null) {
                logger.error(String.format("Received null move message for play message [%s]", playMsg.toString()));
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
