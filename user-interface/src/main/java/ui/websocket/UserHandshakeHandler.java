package ui.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import ui.security.JwtTokenProvider;

import java.security.Principal;
import java.util.Map;

public class UserHandshakeHandler extends DefaultHandshakeHandler {
    private static final Logger log = LoggerFactory.getLogger(UserHandshakeHandler.class);
    private static final String ATTR_PRINCIPAL = "__principal__";

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String name = (String) attributes.get(ATTR_PRINCIPAL);
        String token = request.getHeaders().get("Authentication").toString();
        log.info("Assigned username " + name + " of token: " + token);
        return () -> name;
    }
}
