package pm.kafka;

import message.DefaultKafkaMessage;
import message.created.PlayMessage;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import pm.transformer.PlayTransformer;
import serde.PlayMessageSerde;

import java.util.function.Consumer;

@SuppressWarnings("rawtypes")
@Component
public class PlayStateStoreConfig {
    private final String playStateStoreName = "play-state-store";

    //State stores
    @Bean
    private StoreBuilder playStateStoreBuilder() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(playStateStoreName), Serdes.String(), new PlayMessageSerde());
    }

    @Bean
    public Consumer<KStream<String, DefaultKafkaMessage>> processStatePlay() {
        return stream -> stream
                .mapValues(value -> value == null
                        ? null
                        : (PlayMessage) value.retrieve(PlayMessage.class.getCanonicalName()))
                .transform(() -> new PlayTransformer(playStateStoreName), playStateStoreName);
    }
}
