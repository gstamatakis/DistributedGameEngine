package pm.kafka;

import message.DefaultKafkaMessage;
import message.created.JoinedPlayMoveMessage;
import message.created.MoveMessage;
import message.created.PlayMessage;
import org.apache.kafka.common.serialization.Serde;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import serde.DefaultKafkaMessageSerde;
import serde.JoinedPlayMoveMessageSerde;
import serde.MoveMessageSerde;
import serde.PlayMessageSerde;

/**
 * SerDes for the Kafka Stream transformers.
 */
@Component
public class Serdes {
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

    @Bean
    public Serde<DefaultKafkaMessage> DefaultKafkaMessageSerde() {
        return new DefaultKafkaMessageSerde();
    }
}
