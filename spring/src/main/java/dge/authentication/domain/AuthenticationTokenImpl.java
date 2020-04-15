package dge.authentication.domain;

import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.DigestUtils;

import java.util.Collection;

@Slf4j
@ToString(callSuper = true)
public class AuthenticationTokenImpl extends AbstractAuthenticationToken {

    @Setter
    private final String username;

    public AuthenticationTokenImpl(String principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.username = principal;
    }

    public void authenticate() {
        setAuthenticated(getDetails() != null && getDetails() instanceof SessionUser && !((SessionUser) getDetails()).hasExpired());
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return username != null ? username.toString() : "";
    }

    public String getHash() {
        return DigestUtils.md5DigestAsHex(String.format("%s_%d", username, ((SessionUser) getDetails()).getCreated().getTime()).getBytes());
    }

}
