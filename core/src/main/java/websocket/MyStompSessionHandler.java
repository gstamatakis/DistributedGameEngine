package websocket;

import com.google.gson.Gson;
import message.completed.CompletedMoveMessage;
import message.created.PlayMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

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

    public MyStompSessionHandler(List<String> subs, ArrayBlockingQueue<DefaultSTOMPMessage> queue, BufferedWriter output) {
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
        logger.error("handleException: " + exception.getMessage());
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        logger.error("handleTransportError: " + exception.getMessage());
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return DefaultSTOMPMessage.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        DefaultSTOMPMessage msg = (DefaultSTOMPMessage) payload;
        msg.setAckID(headers.getAck());

        try {
            printNonInteractiveMessage(msg, output);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        if (!receivedMessageQueue.offer(msg)) {
            logger.warn("Queue of received STOMP messages is full! Discarding messages!");
        }
    }

    private synchronized void printNonInteractiveMessage(DefaultSTOMPMessage srvMessage, BufferedWriter output) throws IOException {
        switch (srvMessage.getMessageType()) {
            case NOTIFICATION:
                output.write("\nNOTIFICATION: " + srvMessage.getPayload());
                break;
            case MOVE_DENIED:
                CompletedMoveMessage deniedMoveMessage = gson.fromJson(srvMessage.getPayload(), CompletedMoveMessage.class);
                output.write("\nMove denied:  " + deniedMoveMessage.getMoveMessage());
                break;
            case FETCH_PLAY:
                PlayMessage playMessage = gson.fromJson(srvMessage.getPayload(), PlayMessage.class);
                if (playMessage == null) {
                    output.write("\nRetrieved null play..");
                } else {
                    output.write("\nRetrieved play: " + playMessage.toString());
                }
                break;
            case NEW_MOVE:
            case MOVE_ACCEPTED:
                CompletedMoveMessage successfulMoveMessage = gson.fromJson(srvMessage.getPayload(), CompletedMoveMessage.class);
                output.write("\nNew move:  " + successfulMoveMessage.getMoveMessage());
                break;
            case ERROR:
                output.write("\nERROR: " + srvMessage.getPayload());
                break;
            case KEEP_ALIVE:
                break;
            default:
                //Ignore interactive messages
        }

        //Write to stream before exiting
        output.flush();
    }
}
