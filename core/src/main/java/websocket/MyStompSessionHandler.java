package websocket;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;

import java.io.BufferedWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Queue;

@SuppressWarnings("NullableProblems")
public class MyStompSessionHandler implements StompSessionHandler {
    private static final Logger logger = LoggerFactory.getLogger(MyStompSessionHandler.class);
    private final Queue<DefaultSTOMPMessage> receivedMessageQueue;
    private List<String> subs;
    private BufferedWriter output;
    private final Gson gson;

    public MyStompSessionHandler(List<String> subs, Queue<DefaultSTOMPMessage> queue) {
        this.subs = subs;
        this.receivedMessageQueue = queue;
        this.gson = new Gson();
    }

    public MyStompSessionHandler(List<String> subs, Queue<DefaultSTOMPMessage> queue, BufferedWriter output) {
        this(subs, queue);
        this.output = output;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        for (String sub : subs) {
            session.subscribe(sub, this);
        }
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.error(String.format("handleException [%s] with payload [%s]", exception.getMessage(), new String(payload)));
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        logger.error(String.format("handleTransportError [%s] with session [%s]", exception.getMessage(), session.toString()));
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return DefaultSTOMPMessage.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        DefaultSTOMPMessage msg = (DefaultSTOMPMessage) payload;
        msg.setAckID(headers.getAck());

        if (!receivedMessageQueue.offer(msg)) {
            logger.warn("Queue of received STOMP messages is full! Discarding messages!");
        }
    }
}
