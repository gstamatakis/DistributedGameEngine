package dge.authentication.jwt;

import dge.authentication.domain.AuthenticationTokenImpl;
import dge.authentication.domain.SessionUser;
import dge.authentication.service.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.token.Sha512DigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TokenAuthenticationService {
    private final RedisService service;
    private final long EXPIRATION_TIME = 5 * 60 * 1000; // 5 min
    private final String secret;
    private final String tokenPrefix = "Bearer";
    private final String headerString = "Authorization";


    public TokenAuthenticationService(RedisService service, String key) {
        this.service = service;
        secret = Sha512DigestUtils.shaHex(key);
    }

    public void addAuthentication(HttpServletResponse response, AuthenticationTokenImpl auth) {
        // We generate a token now.
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", auth.getPrincipal());
        claims.put("hash", auth.getHash());
        String JWT = Jwts.builder()
                .setSubject(auth.getPrincipal().toString())
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
        response.addHeader(headerString, tokenPrefix + " " + JWT);
    }

    public Authentication getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(headerString);
        if (token == null) {
            return null;
        }
        //remove "Bearer" text
        token = token.replace(tokenPrefix, "").trim();

        //Validating the token
        if (!token.isEmpty()) {
            Claims claims;
            try {
                claims = Jwts.parser()
                        .setSigningKey(secret)
                        .parseClaimsJws(token).getBody();

            } catch (Exception e) {
                return null;
            }

            //Valid token and now checking to see if the token is actually expired or alive by querying in redis.
            if (claims != null && claims.containsKey("username")) {
                String username = claims.get("username").toString();
                String hash = claims.get("hash").toString();
                SessionUser user = (SessionUser) service.getValue(String.format("%s:%s", username, hash), SessionUser.class);
                if (user != null) {
                    AuthenticationTokenImpl auth = new AuthenticationTokenImpl(user.getUsername(), Collections.emptyList());
                    auth.setDetails(user);
                    auth.authenticate();
                    return auth;
                } else {
                    return new UsernamePasswordAuthenticationToken(null, null);
                }

            }
        }
        return null;
    }
}
