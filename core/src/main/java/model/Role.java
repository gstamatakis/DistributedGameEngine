package model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_ADMIN, ROLE_OFFICIAL, ROLE_CLIENT;

    public String getAuthority() {
        return name();
    }

}
