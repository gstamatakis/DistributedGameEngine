package pm.kafka;

import message.DefaultKafkaMessage;
import message.completed.CompletedMoveMessage;
import message.completed.CompletedPlayMessage;
import message.created.JoinedPlayMoveMessage;
import message.created.MoveMessage;
import message.created.PlayMessage;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import pm.transformer.PlayTransformer;
import serde.DefaultKafkaMessageSerde;
import serde.JoinedPlayMoveMessageSerde;
import serde.MoveMessageSerde;
import serde.PlayMessageSerde;

import java.util.function.BiFunction;

@SuppressWarnings({"rawtypes", "unchecked"})
@Component
public class KafkaStreamsConfig {
    private final String playStateStoreName = "play-state-store";
    private final String completedPlaysStoreName = "completed-play-ids";

    //State stores
    @Bean
    private StoreBuilder playStateStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(playStateStoreName), Serdes.String(), new PlayMessageSerde());
    }

    @Bean
    private StoreBuilder completedPlaysStoreStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(completedPlaysStoreName), Serdes.String(), Serdes.String());
    }

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
                    .transform(() -> new PlayTransformer(playStateStoreName, completedPlaysStoreName), playStateStoreName, completedPlaysStoreName);

            //Send to output topics separately
            return processedMoves.branch(
                    (id, msg) -> msg.isType(CompletedMoveMessage.class.getCanonicalName()),
                    (id, msg) -> msg.isType(CompletedPlayMessage.class.getCanonicalName()));
        };
    }
}
