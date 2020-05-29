package ui.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 9999)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/play").setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/app");
        config.enableSimpleBroker("/topic", "/queue");
    }

    @EventListener
    public void handleConnectEvent(SessionConnectEvent event) {
        if (event.getUser() != null) {
            log.info("===> handleConnectEvent: username=" + event.getUser().getName() + ", event=" + event.toString());
        }
    }

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        if (event.getUser() != null) {
            log.info("<==> handleSubscribeEvent: username=" + event.getUser().getName() + ", event=" + event.toString());
        }
    }

    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        if (event.getUser() != null) {
            log.info("<=== handleDisconnectEvent: username=" + event.getUser().getName() + ", event=" + event.toString());
        }
    }


}