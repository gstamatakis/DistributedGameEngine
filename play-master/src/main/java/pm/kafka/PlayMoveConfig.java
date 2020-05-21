package pm.kafka;

import message.DefaultKafkaMessage;
import message.completed.CompletedMoveMessage;
import message.completed.CompletedPlayMessage;
import message.created.JoinedPlayMoveMessage;
import message.created.PlayMessage;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import pm.transformer.PlayMoveTransformer;

import java.util.function.BiFunction;

@SuppressWarnings("unchecked")
@Component
public class PlayMoveConfig {

    @Bean
    public BiFunction<KStream<String, DefaultKafkaMessage>, KTable<String, DefaultKafkaMessage>, KStream<String, DefaultKafkaMessage>[]> processPlaysAndMoves() {
        return (moveStream, playTable) -> {
            //Inner-Join the player moves with the available plays
            KStream<String, DefaultKafkaMessage> processedMoves = moveStream
                    .join(playTable, JoinedPlayMoveMessage::new)
                    .transform(PlayMoveTransformer::new);

            //Send to output topics separately
            return processedMoves.branch(
                    (id, msg) -> msg.isType(PlayMessage.class.getCanonicalName()),  //On-going plays -> Back to new-plays
                    (id, msg) -> msg.isType(CompletedMoveMessage.class.getCanonicalName()), //Finished plays -> Send to completed-plays
                    (id, msg) -> msg.isType(CompletedPlayMessage.class.getCanonicalName()));//Processed moves -> Send to completed-moves
        };
    }
}
