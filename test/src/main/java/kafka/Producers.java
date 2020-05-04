package kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.*;


public class Producers {

    public static void main(String... args) {
        Random rng = new Random(0);
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Arrays.asList("127.0.0.1:9094", "127.0.0.1:9095", "127.0.0.1:9096"));
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        DefaultKafkaProducerFactory<String, String> pf = new DefaultKafkaProducerFactory<>(props);

        //Default game state in TTT (different in each game)
        List<KeyValue<String, String>> defaultActions = Arrays.asList(
                new KeyValue<>("", "1"),
                new KeyValue<>("", "2"),
                new KeyValue<>("", "3"),
                new KeyValue<>("", "4"),
                new KeyValue<>("", "5"),
                new KeyValue<>("", "6"),
                new KeyValue<>("", "7"),
                new KeyValue<>("", "8"),
                new KeyValue<>("", "9")
        );
        KafkaTemplate<String, String> defTemplate = new KafkaTemplate<>(pf, true);
        defTemplate.setDefaultTopic("kafka-chat");
        for (KeyValue<String, String> defaultAction : defaultActions) {
            defTemplate.sendDefault(defaultAction.key, defaultAction.value);
        }

        //Send player actions
        List<KeyValue<String, String>> p1Actions = Arrays.asList(
                new KeyValue<>("p1", "5"),
                new KeyValue<>("p1", "1"),
                new KeyValue<>("p1", "9")
        );
        List<KeyValue<String, String>> p2Actions = Arrays.asList(
                new KeyValue<>("p2", "5"),
                new KeyValue<>("p2", "1"),
                new KeyValue<>("p2", "9")
        );
        KafkaTemplate<String, String> template1 = new KafkaTemplate<>(pf, true);
        template1.setDefaultTopic("player-actions");
        for (int i = 0; i < p2Actions.size(); i++) {
            KeyValue<String, String> record1 = p1Actions.get(i);
            KeyValue<String, String> record2 = p2Actions.get(i);
            try {
                Thread.sleep(rng.nextInt(1000));
            } catch (InterruptedException ignored) {
            }
            template1.sendDefault(0, record1.key, record1.value);
            try {
                Thread.sleep(rng.nextInt(1000));
            } catch (InterruptedException ignored) {
            }
            template1.sendDefault(0, record2.key, record2.value);
        }
    }

}