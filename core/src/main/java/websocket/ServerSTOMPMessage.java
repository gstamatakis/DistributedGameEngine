package websocket;

import java.io.Serializable;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerSTOMPMessage implements Serializable {
    private String principal;
    private String payload;
    private String time;
    private STOMPMessageType messageType;
    private String ack;

    public ServerSTOMPMessage() {
        this.principal = "";
        this.payload = "";
        this.time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        this.messageType = STOMPMessageType.KEEP_ALIVE;
        this.ack = null;
    }

    public ServerSTOMPMessage(STOMPMessageType type) {
        this();
        this.messageType = type;
    }


    public ServerSTOMPMessage(String payload, STOMPMessageType type) {
        this();
        this.payload = payload;
        this.messageType = type;
    }

    public ServerSTOMPMessage(Principal principal, String payload, STOMPMessageType type) {
        this();
        this.principal = principal.getName();
        this.payload = payload;
        this.messageType = type;
    }


    @Override
    public String toString() {
        return "ServerSTOMPMessage{" +
                "principal=" + principal +
                ", payload='" + payload + '\'' +
                ", time='" + time + '\'' +
                ", messageType=" + messageType +
                '}';
    }


    public String getPayload() {
        return payload;
    }

    public String getTime() {
        return time;
    }

    public String getPrincipal() {
        return principal;
    }

    public STOMPMessageType getMessageType() {
        return messageType;
    }

    public String getAck() {
        return ack;
    }

    public void setAckID(String ackID) {
        this.ack = ackID;
    }
}
