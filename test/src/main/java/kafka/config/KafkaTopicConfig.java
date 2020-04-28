package kafka.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value(value = "${spring.cloud.stream.kafka.binder.brokers}")
    private String bootstrapAddress;

    @Value(value = "${deployment.max-parallelism}")
    private int defaultTopicPartitions;

    @Value(value = "${deployment.replication}")
    private short defaultReplication;


    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic topicPlayerActions() {
        return new NewTopic("player-actions", defaultTopicPartitions, defaultReplication);
    }


}
