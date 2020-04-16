package authentication.resource;

import authentication.AuthenticationTokenImpl;
import authentication.service.RedisService;
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

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(AuthenticationTokenImpl auth, HttpServletResponse response) {
        service.setValue(auth.getPrincipal().toString().toLowerCase(), "");
        return "Logout Successfully";
    }

}
