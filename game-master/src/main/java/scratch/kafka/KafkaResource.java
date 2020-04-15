package scratch.kafka;

import authentication.AuthenticationTokenImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/kafka")
public class KafkaResource {
    private final KafkaService service;

    public KafkaResource(KafkaService service) {
        this.service = service;
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public String kafkaStatus(AuthenticationTokenImpl auth, HttpServletResponse response) {
        return "STATUS_OK!";
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String testKafka(AuthenticationTokenImpl auth, HttpServletResponse response) {
        try {
            return service.kafkaTest();
        } catch (InterruptedException e) {
            return e.getMessage();
        }
    }
}
