package ui.kafka;

import message.DefaultKafkaMessage;
import message.created.TournamentPlayMessage;
import message.queue.PracticeQueueMessage;
import message.queue.JoinTournamentQueueMessage;
import message.queue.CreateTournamentQueueMessage;
import message.requests.RequestJoinTournamentMessage;
import message.requests.RequestPracticeMessage;
import serde.*;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value(value = "${spring.cloud.stream.kafka.binder.brokers}")
    private String bootstrapAddress;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public KafkaTemplate<String, DefaultKafkaMessage> DefaultPlayMessageTemplate() {
        Map<String, Object> cfg = producerConfigs();
        cfg.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DefaultKafkaMessageSerde.class);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(cfg));
    }

    @Bean
    public KafkaTemplate<String, PracticeQueueMessage> PracticeQueueMessageTemplate() {
        Map<String, Object> cfg = producerConfigs();
        cfg.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, PracticeQueueMessageSerde.class);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(cfg));
    }

    @Bean
    public KafkaTemplate<String, JoinTournamentQueueMessage> JoinTournamentQueueMessageTemplate() {
        Map<String, Object> cfg = producerConfigs();
        cfg.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JoinTournamentQueueMessageSerde.class);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(cfg));
    }

    @Bean
    public KafkaTemplate<String, CreateTournamentQueueMessage> CreateTournamentQueueMessageTemplate() {
        Map<String, Object> cfg = producerConfigs();
        cfg.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CreateTournamentQueueMessageSerde.class);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(cfg));
    }

    @Bean
    public KafkaTemplate<String, RequestJoinTournamentMessage> RequestJoinTournamentMessageTemplate() {
        Map<String, Object> cfg = producerConfigs();
        cfg.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, RequestJoinTournamentMessageSerde.class);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(cfg));
    }

    @Bean
    public KafkaTemplate<String, RequestPracticeMessage> RequestPracticeMessageTemplate() {
        Map<String, Object> cfg = producerConfigs();
        cfg.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, RequestPracticeMessageSerde.class);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(cfg));
    }

    @Bean
    public KafkaTemplate<String, TournamentPlayMessage> TournamentPlayMessageTemplate() {
        Map<String, Object> cfg = producerConfigs();
        cfg.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, TournamentPlayMessageSerde.class);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(cfg));
    }
}
