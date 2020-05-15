package ui.kafka;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 1000)
public class KafkaTopicConfig {

    @Value(value = "${spring.cloud.stream.kafka.binder.brokers}")
    private String bootstrapAddress;

    @Value(value = "${spring.cloud.stream.kafka.binder.replication-factor}")
    private short replicationFactor;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    //String name, int numPartitions, short replicationFactor
    @Bean
    public NewTopic newPlaysTopic() {
        return new NewTopic("new-plays", 1, replicationFactor);
    }

    @Bean
    public NewTopic finishedPlaysTopic() {
        return new NewTopic("completed-plays", 1, replicationFactor);
    }

    @Bean
    public NewTopic joinPlayTopic() {
        return new NewTopic("join-play", 1, replicationFactor);
    }

    @Bean
    public NewTopic errorTopic() {
        return new NewTopic("errors", 1, replicationFactor);
    }
}
