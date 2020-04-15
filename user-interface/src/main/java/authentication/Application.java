package authentication;

import authentication.jwt.TokenAuthenticationService;
import authentication.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(
        exclude = {RedisRepositoriesAutoConfiguration.class}
)
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