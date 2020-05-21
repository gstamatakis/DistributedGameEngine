package gm.kafka;

import gm.transformer.CreateTournamentTransformer;
import gm.transformer.JoinTournamentTransformer;
import gm.transformer.PracticeQueueTransformer;
import message.DefaultKafkaMessage;
import message.created.PlayMessage;
import message.queue.CreateTournamentQueueMessage;
import message.queue.JoinTournamentQueueMessage;
import message.queue.PracticeQueueMessage;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import serde.*;

import java.util.function.Function;

@Component
@SuppressWarnings({"rawtypes", "unchecked"})
public class KafkaStreamsConfig {
    private final String pairPracticePlayersStore = "pair-practice-players-store";
    private final String pairTournamentPlayersStore = "pair-tournament-players-store";
    private final String userToGameIDStore = "user-to-playID";
    private final String gameIDToGameStore = "playID-to-game";
    private final String practiceScoreStore = "practice-score-store";
    private final String tournamentScoreStore = "tournament-score-store";

    //State stores
    @Bean
    public StoreBuilder pairPracticePlayersStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(pairPracticePlayersStore), Serdes.String(), new PracticeQueueMessageSerde());
    }

    @Bean
    public StoreBuilder pairTournamentPlayersStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(pairTournamentPlayersStore), Serdes.String(), new TournamentPlayMessageSerde());
    }

    @Bean
    public StoreBuilder userToGameIDStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(userToGameIDStore), Serdes.String(), new PlayMessageSerde());
    }

    @Bean
    public StoreBuilder gameIDToGameStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(gameIDToGameStore), Serdes.String(), new PlayMessageSerde());
    }

    @Bean
    public StoreBuilder practiceScoreStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(practiceScoreStore), Serdes.String(), new PracticePlaysScoreSerde());
    }

    @Bean
    public StoreBuilder tournamentScoreStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(tournamentScoreStore), Serdes.String(), new TournamentPlaysScoreSerde());
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
    public Serde<CreateTournamentQueueMessage> CreateTournamentQueueMessageSerde() {
        return new CreateTournamentQueueMessageSerde();
    }

    @Bean
    public Serde<JoinTournamentQueueMessage> JoinTournamentQueueMessageSerde() {
        return new JoinTournamentQueueMessageSerde();
    }

    @Bean
    public Serde<PlayMessage> PlayMessageSerde() {
        return new PlayMessageSerde();
    }


    /**
     * Kafka Streams topology for creating Tournament plays and pairing players for Practice plays.
     *
     * @return
     */
    @Bean
    public Function<KStream<String, DefaultKafkaMessage>, KStream<String, DefaultKafkaMessage>> processUsersJoinGame() {
        return stream -> {
            //Branch to tournaments and non-tournaments
            KStream<String, DefaultKafkaMessage>[] forks = stream.branch(
                    (id, msg) -> msg.isType(PracticeQueueMessage.class.getCanonicalName()),
                    (id, msg) -> msg.isType(JoinTournamentQueueMessage.class.getCanonicalName()),
                    (id, msg) -> msg.isType(CreateTournamentQueueMessage.class.getCanonicalName()));

            //Map to the original objects
            KStream<String, PracticeQueueMessage> practiceStream =
                    forks[0].map((key, value) -> new KeyValue<>(key, (PracticeQueueMessage) value.retrieve(PracticeQueueMessage.class.getCanonicalName())));

            KStream<String, JoinTournamentQueueMessage> joinTournamentStream =
                    forks[1].map((key, value) -> new KeyValue<>(key, (JoinTournamentQueueMessage) value.retrieve(JoinTournamentQueueMessage.class.getCanonicalName())));

            KStream<String, CreateTournamentQueueMessage> createTournamentStream =
                    forks[2].map((key, value) -> new KeyValue<>(key, (CreateTournamentQueueMessage) value.retrieve(CreateTournamentQueueMessage.class.getCanonicalName())));

            //Handle practice play pairs
            KStream<String, DefaultKafkaMessage> practiceBranch = practiceStream.transform(
                    () -> new PracticeQueueTransformer(pairPracticePlayersStore, userToGameIDStore, gameIDToGameStore),
                    pairPracticePlayersStore, userToGameIDStore, gameIDToGameStore);

            //Handle the tournament creation process
            createTournamentStream.transform(
                    () -> new CreateTournamentTransformer(pairTournamentPlayersStore),
                    pairTournamentPlayersStore, userToGameIDStore, gameIDToGameStore);

            //Handle the tournament pairs
            KStream<String, DefaultKafkaMessage> tournamentBranch = joinTournamentStream.transform(
                    () -> new JoinTournamentTransformer(pairTournamentPlayersStore, userToGameIDStore, gameIDToGameStore),
                    pairTournamentPlayersStore, userToGameIDStore, gameIDToGameStore);

            //Merge everything into a single stream and output the result to the specified topic
            return practiceBranch.merge(tournamentBranch);
        };
    }
}