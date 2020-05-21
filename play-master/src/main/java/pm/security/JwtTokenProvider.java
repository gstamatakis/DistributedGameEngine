package pm.security;

import exception.CustomException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class JwtTokenProvider {

    @Value("${security.jwt.token.secret-key}")
    private String secretKey;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }


    public Authentication getAuthentication(String token) {
        UserDetails userDetails = getUserDetails(getUsername(token), getRole(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private UserDetails getUserDetails(String username, Role role) {
        return org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password("")   //TODO consider a pre-authenticated token
                .authorities(role)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    public Role getRole(String token) {
        List<Map<String, String>> result = (List<Map<String, String>>) Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().get("auth");
        return Role.valueOf(result.get(0).get("authority"));
    }

    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null) {
            if (bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            } else {
                return bearerToken;
            }
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException("Expired or invalid JWT token", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
