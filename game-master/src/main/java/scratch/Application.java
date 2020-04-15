package scratch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import scratch.kafka.KafkaService;

@SpringBootApplication
public class Application {

    @Autowired
    private KafkaService kafkaService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}