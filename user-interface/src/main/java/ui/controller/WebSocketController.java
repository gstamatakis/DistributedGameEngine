package ui.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import websocket.ClientSTOMPMessage;
import websocket.STOMPMessageType;
import websocket.ServerSTOMPMessage;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Controller for all the WebSockets.
 * <p>
 * The annotation @SendToUser indicates that the return value of a message-handling method should be sent as a Message
 * to the specified destination(s) prepended with “/user/{username}“.
 */
@Controller
public class WebSocketController {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/echo")
    @SendToUser("/queue/reply")
    public ServerSTOMPMessage echo(@Payload ClientSTOMPMessage clientSTOMPMessage, Principal principal) {
        final String time = new SimpleDateFormat("HH:mm").format(new Date());
        return new ServerSTOMPMessage(principal, clientSTOMPMessage.getPayload(), time, STOMPMessageType.NOTIFICATION);
    }

    @MessageMapping("/broadcast")
    @SendToUser("/topic/broadcast")
    public ServerSTOMPMessage broadcast(@Payload ClientSTOMPMessage clientSTOMPMessage, Principal principal) {
        final String time = new SimpleDateFormat("HH:mm").format(new Date());
        return new ServerSTOMPMessage(principal, clientSTOMPMessage.getPayload(), time, STOMPMessageType.NOTIFICATION);
    }

    @MessageMapping("/move")
    public void handleMoves(@Payload ClientSTOMPMessage clientSTOMPMessage, Principal principal) {
        final String time = new SimpleDateFormat("HH:mm").format(new Date());
        logger.info(principal + " | " + clientSTOMPMessage.toString());
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }

}
