package pm.kafka;

import message.DefaultKafkaMessage;
import message.created.JoinedPlayMoveMessage;
import message.created.MoveMessage;
import message.created.PlayMessage;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import pm.transformer.PlayTransformer;
import serde.DefaultKafkaMessageSerde;
import serde.JoinedPlayMoveMessageSerde;
import serde.MoveMessageSerde;
import serde.PlayMessageSerde;

import java.util.function.BiFunction;

@Component
public class KafkaStreamsConfig {

    //Serdes
    @Bean
    public Serde<PlayMessage> PlayMessageSerde() {
        return new PlayMessageSerde();
    }

    @Bean
    public Serde<MoveMessage> MoveMessageSerde() {
        return new MoveMessageSerde();
    }

    @Bean
    public Serde<JoinedPlayMoveMessage> JoinedPlayMoveMessageSerde() {
        return new JoinedPlayMoveMessageSerde();
    }

    @Bean
    public Serde<DefaultKafkaMessage> DefaultKafkaMessageSerde() {
        return new DefaultKafkaMessageSerde();
    }

    //Kafka streams topology
    @Bean
    public BiFunction<KStream<String, MoveMessage>, KTable<String, PlayMessage>, KStream<String, DefaultKafkaMessage>[]> processPlaysAndMoves() {
        return (moveStream, playTable) -> {
            //Inner-Join the player moves with the available plays
            KStream<String, DefaultKafkaMessage> processedMoves = moveStream
                    .join(playTable, JoinedPlayMoveMessage::new)
                    .transform(PlayTransformer::new);

            //Send to output topics separately
            return processedMoves.branch(
                    (id, msg) -> msg.isCompletedMoveMessage(),
                    (id, msg) -> msg.isCompletedPlayMessage());
        };
    }
}
