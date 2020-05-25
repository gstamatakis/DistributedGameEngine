package gm.kafka;


import message.DefaultKafkaMessage;
import message.completed.CompletedPlayMessage;
import message.completed.UserScore;
import model.PlayTypeEnum;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import serde.PlayScoreSerde;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
@SuppressWarnings({"rawtypes", "unchecked"})
/*
The score of a practice play is recorded seperately from the score obtained in
tournament plays. The number of plays each user participated in, the number
of wins, ties and losses, and the total score of each play are kept.
That is, for each user there are three scores:

Practice scores: The scores of individual plays are available only to the players of these plays (a player can see the full list of practice scores she
played). A player can only see the total score of other players.

Tournament scores: The scores of each individual play are available to all
players.
 */
public class ProcessUserScoresConfig {
    private static final Logger logger = LoggerFactory.getLogger(ProcessUserScoresConfig.class);
    private final String practiceScoreStore = "practice-score-store";
    private final String tournamentScoreStore = "tournament-score-store";

    @Bean
    public StoreBuilder practiceScoreStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(practiceScoreStore), Serdes.String(), new PlayScoreSerde());
    }

    @Bean
    public StoreBuilder tournamentScoreStore() {
        return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(tournamentScoreStore), Serdes.String(), new PlayScoreSerde());
    }

    @Bean
    public Consumer<KStream<String, DefaultKafkaMessage>> processUserScores() {
        return completedPlaysStream -> {
            completedPlaysStream.foreach((key, value) -> logger.info(String.format("Consumed [%s,%s]", key, value.toString())));

            //Map to plays messages
            KStream<String, CompletedPlayMessage> plays = completedPlaysStream.mapValues(value -> (CompletedPlayMessage) value.retrieve(CompletedPlayMessage.class.getCanonicalName()));

            //Branch the stream and handle separately the practice and tournament plays
            KStream<String, CompletedPlayMessage>[] branches = plays.branch(
                    (id, play) -> play.getPlayType() == PlayTypeEnum.PRACTICE,
                    (id, play) -> play.getPlayType() == PlayTypeEnum.TOURNAMENT);

            KStream<String, CompletedPlayMessage> practicePlays = branches[0];
            KStream<String, CompletedPlayMessage> tournamentPlays = branches[1];

            //Update scores for each play type
            practicePlays
                    .flatMap((KeyValueMapper<String, CompletedPlayMessage, Iterable<KeyValue<String, UserScore>>>) (key, value) -> {
                        List<KeyValue<String, UserScore>> result = new ArrayList<>();
                        result.add(KeyValue.pair(value.getP1(), new UserScore(value.getP1(), value)));
                        result.add(KeyValue.pair(value.getP2(), new UserScore(value.getP2(), value)));
                        return result;
                    })
                    .groupByKey()
                    .reduce(UserScore::merge)
                    .mapValues(value -> {
                        logger.info(String.format("PracticeScoresStore: [%s]", value.toString()));
                        return value;
                    })
                    .toStream()
                    .to(practiceScoreStore);

            tournamentPlays
                    .flatMap((KeyValueMapper<String, CompletedPlayMessage, Iterable<KeyValue<String, UserScore>>>) (key, value) -> {
                        List<KeyValue<String, UserScore>> result = new ArrayList<>();
                        result.add(KeyValue.pair(value.getP1(), new UserScore(value.getP1(), value)));
                        result.add(KeyValue.pair(value.getP2(), new UserScore(value.getP2(), value)));
                        return result;
                    })
                    .groupByKey()
                    .reduce(UserScore::merge)
                    .mapValues(value -> {
                        logger.info(String.format("TournamentScoresStore: [%s]", value.toString()));
                        return value;
                    })
                    .toStream()
                    .to(tournamentScoreStore);
        };
    }
}
