package gm.kafka;

import message.JoinPlayMessage;
import message.PlayMessage;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import serde.JoinPlaySerde;
import serde.PlaySerde;

import java.util.function.Function;

@Component
@SuppressWarnings({"rawtypes", "unchecked"})
public class KafkaStreamsConfig {
    private final String pairPracticePlayersStore = "pair-practice-players-store";
    private final String pairTournamentPlayersStore = "pair-tournament-players-store";
    private final String userToGameIDStore = "user-to-gameID";
    private final String gameIDToGameStore = "gameID-to-game";

    //State stores
    @Bean
    public StoreBuilder pairPracticePlayersStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(pairPracticePlayersStore), Serdes.String(), new JoinPlaySerde());
    }

    @Bean
    public StoreBuilder pairTournamentPlayersStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(pairTournamentPlayersStore), Serdes.String(), new JoinPlaySerde());
    }

    @Bean
    public StoreBuilder userToGameIDStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(userToGameIDStore), Serdes.String(), new PlaySerde());
    }

    @Bean
    public StoreBuilder gameIDToGameStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(gameIDToGameStore), Serdes.String(), new PlaySerde());
    }

    //Serdes
    @Bean
    public Serde<JoinPlayMessage> UserJoinQueueMessageSerde() {
        return new JoinPlaySerde();
    }

    @Bean
    public Serde<PlayMessage> PlaySerdeSerde() {
        return new PlaySerde();
    }

    //Kafka streams topology
    @Bean
    public Function<KStream<String, JoinPlayMessage>, KStream<String, PlayMessage>> processUsersJoinGame() {
        return stream -> {

            //Branch to tournaments and non-tournaments
            KStream<String, JoinPlayMessage>[] forks = stream.branch(
                    (id, msg) -> msg.isPracticePlay(),
                    (id, msg) -> msg.isTournamentPlay()
            );

            //Handle practice play pairs
            KStream<String, PlayMessage> practiceBranch = forks[0].transform(
                    () -> new PracticeQueueTransformer(pairPracticePlayersStore, userToGameIDStore, gameIDToGameStore),
                    pairPracticePlayersStore, userToGameIDStore, gameIDToGameStore);

            //Handle the tournament pairs
            KStream<String, PlayMessage> tournamentBranch = forks[1].map((key, value) -> null); //TODO implement this

            //Merge everything into a single stream and output the result to the specified topic
            return practiceBranch.merge(tournamentBranch);
        };
    }
}
