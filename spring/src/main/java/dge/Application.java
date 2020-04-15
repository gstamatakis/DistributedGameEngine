package dge;

import com.fasterxml.jackson.databind.ObjectMapper;
import dge.authentication.jwt.TokenAuthenticationService;
import dge.authentication.service.RedisService;
import dge.kafka.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    @Autowired
    private RedisService redisService;

    @Value("${ENC_KEY}")
    private String encKey;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public TokenAuthenticationService tokenAuthService() {
        return new TokenAuthenticationService(redisService, encKey);
    }

    @Bean
    public ObjectMapper mapper() {
        return new ObjectMapper();
    }
}