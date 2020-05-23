package gm.kafka;


import message.DefaultKafkaMessage;
import message.created.PlayMessage;
import model.PlayTypeEnum;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import serde.PracticePlaysScoreSerde;
import serde.TournamentPlaysScoreSerde;

import java.util.function.Consumer;

@Component
@SuppressWarnings("rawtypes")
public class ProcessUserScoresConfig {
    private final String practiceScoreStore = "practice-score-store";
    private final String tournamentScoreStore = "tournament-score-store";

    @Bean
    public StoreBuilder practiceScoreStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(practiceScoreStore), Serdes.String(), new PracticePlaysScoreSerde());
    }

    @Bean
    public StoreBuilder tournamentScoreStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(tournamentScoreStore), Serdes.String(), new TournamentPlaysScoreSerde());
    }

    @Bean
    public Consumer<KStream<String, DefaultKafkaMessage>> processUserScores() {
        return completedPlaysStream -> {
            //Map to plays messages
            KStream<String, PlayMessage> plays = completedPlaysStream.mapValues(value -> (PlayMessage) value.retrieve(PlayMessage.class.getCanonicalName()));

            //Branch the stream and handle separately the practice and tournament plays
            KStream<String, PlayMessage>[] branches = plays.branch(
                    (id, play) -> play.getPlayTypeEnum() == PlayTypeEnum.PRACTICE,
                    (id, play) -> play.getPlayTypeEnum() == PlayTypeEnum.TOURNAMENT);
            KStream<String, PlayMessage> practicePlays = branches[0];
            KStream<String, PlayMessage> tournamentPlays = branches[1];

            //Update scores for each play type
            practicePlays.map().to(practiceScoreStore);
        };
    }
}
