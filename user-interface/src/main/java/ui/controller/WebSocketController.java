package ui.controller;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import websocket.InputSTOMPMessage;
import websocket.OutputSTOMPMessage;

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
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    private Gson gson = new Gson();

    @MessageMapping("/echo")
    @SendToUser("/queue/reply")
    public OutputSTOMPMessage echo(@Payload InputSTOMPMessage inputSTOMPMessage, Principal principal) {
        final String time = new SimpleDateFormat("HH:mm").format(new Date());
        return new OutputSTOMPMessage(principal, inputSTOMPMessage.getSender(), inputSTOMPMessage.getPayload(), time);
    }

    @MessageMapping("/broadcast")
    @SendToUser("/topic/broadcast")
    public OutputSTOMPMessage broadcast(@Payload InputSTOMPMessage inputSTOMPMessage, Principal principal) {
        final String time = new SimpleDateFormat("HH:mm").format(new Date());
        return new OutputSTOMPMessage(principal, inputSTOMPMessage.getSender(), inputSTOMPMessage.getPayload(), time);
    }


    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }

}
