package dge.authentication.resource;

import dge.authentication.domain.AuthenticationTokenImpl;
import dge.authentication.service.RedisService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/user")
public class UserResource {

    private final RedisService service;

    public UserResource(RedisService service) {
        this.service = service;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String getName(AuthenticationTokenImpl auth, HttpServletResponse response) {
        return auth.getPrincipal().toString();
    }

    @RequestMapping(value = "/processor", method = RequestMethod.GET)
    public Integer getProcessor(AuthenticationTokenImpl auth, HttpServletResponse response) {
        return Runtime.getRuntime().availableProcessors();
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(AuthenticationTokenImpl auth, HttpServletResponse response) {
        service.setValue(auth.getPrincipal().toString().toLowerCase(), "");
        return "Logout Successfully";
    }

}
