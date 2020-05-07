package gm.kafka;

import message.PracticePlayMessage;
import message.UserJoinQueueMessage;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import serde.PracticePlaySerde;
import serde.UserJoinQueueSerde;

import java.util.function.Function;

@Component
@SuppressWarnings({"rawtypes"})
public class KafkaStreamsConfig {
    private final String storeName = "practice-queue-store";

    @Bean
    public StoreBuilder PracticeQueueStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(storeName), Serdes.String(), new UserJoinQueueSerde());
    }

    @Bean
    public Serde<UserJoinQueueMessage> UserJoinQueueMessageSerde() {
        return new UserJoinQueueSerde();
    }

    @Bean
    public Serde<PracticePlayMessage> PracticePlayMessageSerde() {
        return new PracticePlaySerde();
    }

    @Bean
    public Function<KStream<String, UserJoinQueueMessage>, KStream<String, PracticePlayMessage>> processUsersJoinGame() {
        return input -> input
                .transform(() -> new PracticeQueueTransformer(storeName), storeName)
                .filter((k, v) -> v != null && k != null);
    }
}
