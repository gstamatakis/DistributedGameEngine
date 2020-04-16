package authentication.config;


import authentication.AuthenticationTokenImpl;
import authentication.SessionUser;
import authentication.service.RedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AuthenticationProviderImpl implements AuthenticationProvider {

    private final RedisService service;

    @Value("${SESSION_TIMEOUT_MIN}")
    private int sessionTimeoutMin;

    public AuthenticationProviderImpl(RedisService service) {
        this.service = service;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getPrincipal() + "";
        String hashedPassword = String.valueOf(authentication.getCredentials().hashCode());

        SessionUser user = (SessionUser) service.getValue(username, SessionUser.class);
        if (user == null) {
            return null;
        }
        if (!hashedPassword.equals(user.getPassword())) {
            return null;
        }

        user.setCreated(new Date());
        AuthenticationTokenImpl auth = new AuthenticationTokenImpl(user.getUsername(), Collections.emptyList());
        auth.setAuthenticated(true);
        auth.setDetails(user);
        service.setValue(username, user, TimeUnit.MINUTES, sessionTimeoutMin, true);
        return auth;
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(UsernamePasswordAuthenticationToken.class);
    }

}
