package gm.kafka;

import gm.transformer.CreateTournamentTransformer;
import gm.transformer.JoinTournamentTransformer;
import gm.transformer.PracticeQueueTransformer;
import message.DefaultKafkaMessage;
import message.created.PlayMessage;
import message.queue.PracticeQueueMessage;
import message.queue.TournamentQueueMessage;
import message.requests.RequestCreateTournamentMessage;
import message.serde.*;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@SuppressWarnings({"rawtypes", "unchecked"})
public class KafkaStreamsConfig {
    private final String pairPracticePlayersStore = "pair-practice-players-store";
    private final String pairTournamentPlayersStore = "pair-tournament-players-store";
    private final String userToGameIDStore = "user-to-playID";
    private final String gameIDToGameStore = "playID-to-game";

    //State stores
    @Bean
    public StoreBuilder pairPracticePlayersStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(pairPracticePlayersStore), Serdes.String(), new PracticeQueueMessageSerde());
    }

    @Bean
    public StoreBuilder pairTournamentPlayersStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(pairTournamentPlayersStore), Serdes.String(), new TournamentQueueMessageSerde());
    }

    @Bean
    public StoreBuilder userToGameIDStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(userToGameIDStore), Serdes.String(), new PlayMessageSerde());
    }

    @Bean
    public StoreBuilder gameIDToGameStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(gameIDToGameStore), Serdes.String(), new PlayMessageSerde());
    }

    //Serdes
    @Bean
    public Serde<DefaultKafkaMessage> DefaultPlayMessageSerde() {
        return new DefaultKafkaMessageSerde();
    }

    @Bean
    public Serde<PracticeQueueMessage> PracticeQueueMessageSerde() {
        return new PracticeQueueMessageSerde();
    }

    @Bean
    public Serde<TournamentQueueMessage> TournamentQueueMessageSerde() {
        return new TournamentQueueMessageSerde();
    }

    @Bean
    public Serde<RequestCreateTournamentMessage> RequestCreateTournamentMessageSerde() {
        return new RequestCreateTournamentMessageSerde();
    }

    @Bean
    public Serde<PlayMessage> PlayMessageSerde() {
        return new PlayMessageSerde();
    }

    //Kafka streams topology
    @Bean
    public Function<KStream<String, DefaultKafkaMessage>, KStream<String, PlayMessage>> processUsersJoinGame() {
        return stream -> {
            //Branch to tournaments and non-tournaments
            KStream<String, DefaultKafkaMessage>[] forks = stream.branch(
                    (id, msg) -> msg.isPracticeMessage(),
                    (id, msg) -> msg.isJoinTournamentMessage(),
                    (id, msg) -> msg.isCreateTournamentMessage());

            //Map to the original objects
            KStream<String, PracticeQueueMessage> practiceStream =
                    forks[0].map((key, value) -> new KeyValue<>(key, value.getPracticeQueueMessage()));

            KStream<String, TournamentQueueMessage> joinTournamentStream =
                    forks[1].map((key, value) -> new KeyValue<>(key, value.getTournamentQueueMessage()));

            KStream<String, RequestCreateTournamentMessage> createTournamentStream =
                    forks[2].map((key, value) -> new KeyValue<>(key, value.getRequestCreateTournamentMessage()));


            //Handle practice play pairs
            KStream<String, PlayMessage> practiceBranch = practiceStream.transform(
                    () -> new PracticeQueueTransformer(pairPracticePlayersStore, userToGameIDStore, gameIDToGameStore),
                    pairPracticePlayersStore, userToGameIDStore, gameIDToGameStore);

            //Handle the tournament creation process
            createTournamentStream.transform(
                    () -> new CreateTournamentTransformer(pairTournamentPlayersStore),
                    pairTournamentPlayersStore, userToGameIDStore, gameIDToGameStore);

            //Handle the tournament pairs
            KStream<String, PlayMessage> tournamentBranch = joinTournamentStream.transform(
                    () -> new JoinTournamentTransformer(pairTournamentPlayersStore, userToGameIDStore, gameIDToGameStore),
                    pairTournamentPlayersStore, userToGameIDStore, gameIDToGameStore);

            //Merge everything into a single stream and output the result to the specified topic
            return practiceBranch.merge(tournamentBranch);
        };
    }
}
