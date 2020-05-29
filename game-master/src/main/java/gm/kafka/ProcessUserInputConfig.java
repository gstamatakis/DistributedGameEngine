package gm.kafka;

import gm.transformer.CreateTournamentTransformer;
import gm.transformer.JoinTournamentTransformer;
import gm.transformer.PracticeQueueTransformer;
import message.DefaultKafkaMessage;
import message.completed.CompletedTournamentMessage;
import message.queue.CreateTournamentQueueMessage;
import message.queue.JoinTournamentQueueMessage;
import message.queue.PracticeQueueMessage;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import serde.PlayMessageSerde;
import serde.PracticeQueueMessageSerde;
import serde.TournamentPlayMessageSerde;

import java.util.function.Function;

@Component
@SuppressWarnings({"rawtypes", "unchecked"})
public class ProcessUserInputConfig {
    private static final Logger logger = LoggerFactory.getLogger(ProcessUserInputConfig.class);

    private final String pairPracticePlayersStore = "pair-practice-players-store";
    private final String pairTournamentPlayersStore = "pair-tournament-players-store";
    private final String userToPlayIDStore = "user-to-playID";
    private final String playIDToGameStore = "playID-to-game";
    private final String newPlaysTopic = "new-plays";
    private final String completedTournamentsTopic = "completed-tournaments";

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
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(userToPlayIDStore), Serdes.String(), new PlayMessageSerde());
    }

    @Bean
    public StoreBuilder gameIDToGameStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(playIDToGameStore), Serdes.String(), new PlayMessageSerde());
    }


    /**
     * Kafka Streams topology for creating Tournament plays and pairing players for Practice plays.
     *
     * @return
     */
    @Bean
    public Function<KStream<String, DefaultKafkaMessage>, KStream<String, DefaultKafkaMessage>> processUsersJoinGame() {
        return stream -> {
            stream.foreach((key, value) -> logger.info(String.format("processUsersJoinGame: Consumed [%s,%s]", key, value == null ? null : value.toString())));

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
                    () -> new PracticeQueueTransformer(pairPracticePlayersStore, userToPlayIDStore, playIDToGameStore),
                    pairPracticePlayersStore, userToPlayIDStore, playIDToGameStore);

            //Handle the tournament creation process
            createTournamentStream.transform(
                    () -> new CreateTournamentTransformer(pairTournamentPlayersStore),
                    pairTournamentPlayersStore, userToPlayIDStore, playIDToGameStore);

            //Handle the tournament pairs. Send the completed tournaments to a different topic.
            KStream<String, DefaultKafkaMessage> tournamentBranch_mixed = joinTournamentStream.transform(
                    () -> new JoinTournamentTransformer(pairTournamentPlayersStore, userToPlayIDStore, playIDToGameStore),
                    pairTournamentPlayersStore, userToPlayIDStore, playIDToGameStore);
            KStream<String, DefaultKafkaMessage> completedTournaments = tournamentBranch_mixed
                    .filter((key, value) -> value.isType(CompletedTournamentMessage.class.getCanonicalName()));
            KStream<String, DefaultKafkaMessage> ongoingTournaments = tournamentBranch_mixed
                    .filter((key, value) -> !value.isType(CompletedTournamentMessage.class.getCanonicalName()));
            completedTournaments.to(completedTournamentsTopic);


            //Merge everything into a single stream and output the result to the specified topic
            KStream<String, DefaultKafkaMessage> mergedStreams = practiceBranch.merge(ongoingTournaments);

            //Duplicate the stream so it can be forwarded to both new and ongoing plays
            KStream<String, DefaultKafkaMessage> s1 = mergedStreams.filter((key, value) -> true);
            KStream<String, DefaultKafkaMessage> s2 = mergedStreams.filter((key, value) -> true);
            s1.foreach((key, value) -> logger.info(String.format("processUsersJoinGame: Producing to new-plays [%s,%s]", key, value == null ? null : value.toString())));
            s2.foreach((key, value) -> logger.info(String.format("processUsersJoinGame: Producing to ongoing-plays [%s,%s]", key, value == null ? null : value.toString())));

            s1.to(newPlaysTopic);
            return s2;
        };
    }
}