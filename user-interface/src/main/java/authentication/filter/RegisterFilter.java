package authentication.filter;

import authentication.SessionUser;
import authentication.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RegisterFilter extends AbstractAuthenticationProcessingFilter {
    private final RedisService redisService;

    public RegisterFilter(String url, RedisService tokenService) {
        super(new AntPathRequestMatcher(url));
        this.redisService = tokenService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {
        SessionUser user = new ObjectMapper().readValue(request.getInputStream(), SessionUser.class);
        if (!isValidUser(user)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        user.hashPassword();
        redisService.setValue(user.getUsername(), user, true);
        response.setStatus(HttpServletResponse.SC_OK);
        return null;
    }

    /**
     * User validation.
     *
     * @param user The input user to be checked.
     * @return True if the user is valid for registration, false otherwise.
     */
    public boolean isValidUser(SessionUser user) {
        if (user.getUsername().length() < 5) {
            return false;
        }
        if (user.getPassword().length() < 5) {
            return false;
        }
        return redisService.getKeys(user.getUsername()).isEmpty();
    }
}
