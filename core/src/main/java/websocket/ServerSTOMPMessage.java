package websocket;

import java.io.Serializable;
import java.security.Principal;
import java.time.LocalDateTime;

public class ServerSTOMPMessage implements Serializable {
    private String principal;
    private String sender;
    private String payload;
    private String time;
    private STOMPMessageType messageType;
    private String ack;

    public ServerSTOMPMessage() {
    }

    public ServerSTOMPMessage(STOMPMessageType type) {
        this.principal = "";
        this.sender = "";
        this.payload = "";
        this.time = String.valueOf(LocalDateTime.now());
        this.messageType = type;
        this.ack = null;
    }


    public ServerSTOMPMessage(String payload, STOMPMessageType type) {
        this.principal = "";
        this.sender = "";
        this.payload = payload;
        this.time = String.valueOf(LocalDateTime.now());
        this.messageType = type;
        this.ack = null;
    }

    public ServerSTOMPMessage(Principal principal, String payload, String time, STOMPMessageType type) {
        this.principal = principal.getName();
        this.sender = principal.getName();
        this.payload = payload;
        this.time = time;
        this.messageType = type;
        this.ack = null;
    }


    @Override
    public String toString() {
        return "ServerSTOMPMessage{" +
                "principal=" + principal +
                ", sender='" + sender + '\'' +
                ", payload='" + payload + '\'' +
                ", time='" + time + '\'' +
                ", messageType=" + messageType +
                '}';
    }

    public String getSender() {
        return sender;
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
