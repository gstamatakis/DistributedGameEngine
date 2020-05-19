package ui.controller;

import com.google.gson.Gson;
import exception.CustomException;
import message.created.PlayMessage;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import ui.service.PlayService;
import websocket.DefaultSTOMPMessage;
import websocket.STOMPMessageType;

import java.security.Principal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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

    @Autowired
    private PlayService playService;

    @Autowired
    private InteractiveQueryService interactiveQueryService;

    private final Gson gson = new Gson();

    @ExceptionHandler({CustomException.class})
    public ResponseEntity<String> handleConflict(CustomException ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @MessageMapping("/echo")
    @SendToUser("/queue/reply")
    public DefaultSTOMPMessage echo(@Payload DefaultSTOMPMessage clientSTOMPMessage, Principal principal) {
        return new DefaultSTOMPMessage(principal, clientSTOMPMessage.getPayload(), STOMPMessageType.NOTIFICATION, null, clientSTOMPMessage.getID());
    }

    @MessageMapping("/broadcast")
    @SendToUser("/topic/broadcast")
    public DefaultSTOMPMessage broadcast(@Payload DefaultSTOMPMessage clientSTOMPMessage, Principal principal) {
        return new DefaultSTOMPMessage(principal, clientSTOMPMessage.getPayload(), STOMPMessageType.NOTIFICATION, null, clientSTOMPMessage.getID());
    }

    @MessageMapping("/move")
    @SendToUser("/queue/reply")
    public DefaultSTOMPMessage handleMoves(@Payload DefaultSTOMPMessage clientSTOMPMessage, Principal principal)
            throws InterruptedException, ExecutionException, TimeoutException {
        logger.info(String.format("Received message %s from %s", clientSTOMPMessage, principal.getName()));
        String newMove = clientSTOMPMessage.getPayload();
        String playID = clientSTOMPMessage.getID();
        if (playID == null) {
            throw new IllegalStateException(String.format("Invalid playID={%s}", playID));
        }
        playService.sendMoveToPlay(principal.getName(), newMove, playID);
        return new DefaultSTOMPMessage(principal, String.format("Move %s sent.", newMove), STOMPMessageType.NOTIFICATION, null, clientSTOMPMessage.getID());
    }

    @MessageMapping("/play")
    @SendToUser("/queue/reply")
    public DefaultSTOMPMessage retrieveBoard(@Payload DefaultSTOMPMessage clientSTOMPMessage, Principal principal) {
        logger.info(String.format("Received message %s from %s", clientSTOMPMessage, principal.getName()));
        ReadOnlyKeyValueStore<String, PlayMessage> store =
                interactiveQueryService.getQueryableStore("play-moves-store", QueryableStoreTypes.keyValueStore());
        PlayMessage play = store.get(clientSTOMPMessage.getID());
        return new DefaultSTOMPMessage(principal, gson.toJson(play), STOMPMessageType.FETCH_PLAY, null, clientSTOMPMessage.getID());
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public DefaultSTOMPMessage handleException(Throwable exception) {
        return new DefaultSTOMPMessage("SERVER", exception.getMessage(), STOMPMessageType.ERROR, null, null);
    }

}
