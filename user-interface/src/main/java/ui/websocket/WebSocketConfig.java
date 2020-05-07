package ui.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
//                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//
//                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//                    List<String> authorization = accessor.getNativeHeader("X-Authorization");
//                    logger.debug("X-Authorization: {}", authorization);
//                    String accessToken = authorization.get(0).split(" ")[1];
//                    Jwt jwt = jwtDecoder.decode(accessToken);
//                    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
//                    Authentication authentication = converter.convert(jwt);
//                    accessor.setUser(authentication);
//                }

                return message;
            }
        });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/echo");
        registry.addEndpoint("/echo").withSockJS();

        registry.addEndpoint("/chat");
        registry.addEndpoint("/chat").withSockJS();
    }

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        if (event.getUser() != null) {
            log.info("<==> handleSubscribeEvent: username=" + event.getUser().getName() + ", event=" + event);
        }
    }

    @EventListener
    public void handleConnectEvent(SessionConnectEvent event) {
        if (event.getUser() != null) {
            log.info("===> handleConnectEvent: username=" + event.getUser().getName() + ", event=" + event);
        }
    }

    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        if (event.getUser() != null) {
            log.info("<=== handleDisconnectEvent: username=" + event.getUser().getName() + ", event=" + event);
        }
    }
}