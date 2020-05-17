package websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Queue;

@SuppressWarnings("NullableProblems")
public class MyStompSessionHandler implements StompSessionHandler {
    private static final Logger logger = LoggerFactory.getLogger(MyStompSessionHandler.class);
    private List<String> subs;
    private final Queue<ServerSTOMPMessage> receivedMessageQueue;

    public MyStompSessionHandler(List<String> subs,Queue<ServerSTOMPMessage> queue) {
        this.subs = subs;
        this.receivedMessageQueue = queue;
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
        return ServerSTOMPMessage.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        ServerSTOMPMessage msg = (ServerSTOMPMessage) payload;
        msg.setAckID(headers.getAck());
        if (!receivedMessageQueue.offer(msg)) {
            logger.warn("Queue of received STOMP messages is full! Discarding messages!");
        }
    }
}
