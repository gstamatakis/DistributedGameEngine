package pm.kafka;

import message.completed.CompletedMoveMessage;
import message.created.JoinedPlayMoveMessage;
import message.created.MoveMessage;
import message.created.PlayMessage;
import serde.JoinedPlayMoveMessageSerde;
import serde.MoveMessageSerde;
import serde.PlayMessageSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import pm.transformer.PlayTransformer;

import java.util.function.BiFunction;

@SuppressWarnings("rawtypes")
@Component
public class KafkaStreamsConfig {
    private final String playStateStoreName = "play-moves-store";

    //State stores
    @Bean
    private StoreBuilder playStateStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(playStateStoreName), Serdes.String(), new PlayMessageSerde());
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

    //Kafka streams topology
    @Bean
    public BiFunction<KStream<String, MoveMessage>, KTable<String, PlayMessage>, KStream<String, CompletedMoveMessage>> processPlaysAndMoves() {
        return (moveStream, playTable) -> {
            //Inner-Join the player moves with the available plays
            //Update the play state based on incoming moves
            return moveStream
                    .join(playTable, JoinedPlayMoveMessage::new)
                    .transform(() -> new PlayTransformer(playStateStoreName), playStateStoreName);
        };
    }
}
