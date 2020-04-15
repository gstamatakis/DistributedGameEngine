package authentication.config;


import authentication.AuthenticationTokenImpl;
import authentication.SessionUser;
import authentication.service.RedisService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AuthenticationProviderImpl implements org.springframework.security.authentication.AuthenticationProvider {

    private final RedisService service;

    public AuthenticationProviderImpl(RedisService service) {
        this.service = service;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getPrincipal() + "";
        String password = authentication.getCredentials() + "";

        if (username.length() < 5) {
            throw new BadCredentialsException("Username not found.");
        }
        if (password.length() < 5) {
            throw new BadCredentialsException("Wrong password.");
        }

        //Right now just authenticate on the basis of the user=pass
        if (username.equalsIgnoreCase(password)) {
            SessionUser u = new SessionUser();
            u.setUsername(username);
            u.setCreated(new Date());
            AuthenticationTokenImpl auth = new AuthenticationTokenImpl(u.getUsername(), Collections.emptyList());
            auth.setAuthenticated(true);
            auth.setDetails(u);
            service.setValue(String.format("%s:%s", u.getUsername().toLowerCase(), auth.getHash()), u, TimeUnit.SECONDS, 3600L, true);
            return auth;
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(UsernamePasswordAuthenticationToken.class);
    }

}
