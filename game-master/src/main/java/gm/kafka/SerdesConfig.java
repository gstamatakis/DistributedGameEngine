package gm.kafka;

import message.DefaultKafkaMessage;
import message.created.PlayMessage;
import message.queue.CreateTournamentQueueMessage;
import message.queue.JoinTournamentQueueMessage;
import message.queue.PracticeQueueMessage;
import message.score.PracticePlaysScore;
import message.score.TournamentPlayScore;
import org.apache.kafka.common.serialization.Serde;
import org.springframework.context.annotation.Bean;
import serde.*;

public class SerdesConfig {
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

    @Bean
    public Serde<PracticePlaysScore> PracticePlaysScoreSerde() {
        return new PracticePlaysScoreSerde();
    }

    @Bean
    public Serde<TournamentPlayScore> TournamentPlayScoreSerde() {
        return new TournamentPlaysScoreSerde();
    }
}
