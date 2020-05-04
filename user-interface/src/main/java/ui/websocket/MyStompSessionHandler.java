package ui.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import websocket.OutputMessage;

import java.lang.reflect.Type;
import java.util.List;

public class MyStompSessionHandler implements StompSessionHandler {
    private static final Logger logger = LoggerFactory.getLogger(MyStompSessionHandler.class);
    private List<String> subs;

    public MyStompSessionHandler(List<String> subs) {
        this.subs = subs;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        for (String sub : subs) {
            session.subscribe(sub, this);
        }
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.error(exception.getMessage());
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        logger.error(exception.getMessage());
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return OutputMessage.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        OutputMessage msg = (OutputMessage) payload;
        logger.info("Received: " + msg);
    }
}
