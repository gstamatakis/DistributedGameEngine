package dge.kafka;

import dge.authentication.domain.AuthenticationTokenImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

//@RestController
//@RequestMapping("/kafka")
public class KafkaResource {
    private final KafkaService service;

    public KafkaResource(KafkaService service) {
        this.service = service;
    }

//    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String testKafka(AuthenticationTokenImpl auth, HttpServletResponse response) {
        return "TEST_OK!";
    }
}
