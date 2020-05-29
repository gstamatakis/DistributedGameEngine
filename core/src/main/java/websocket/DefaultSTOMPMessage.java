package websocket;

import java.io.Serializable;
import java.security.Principal;
import java.time.LocalDateTime;

public class DefaultSTOMPMessage implements Serializable {
    private String principal;
    private String payload;
    private String time;
    private STOMPMessageType messageType;
    private String ack;
    private String ID;

    public DefaultSTOMPMessage() {
    }

    public DefaultSTOMPMessage(Principal principal, String payload, STOMPMessageType messageType, String ack, String ID) {
        this.principal = principal.getName();
        this.payload = payload;
        this.messageType = messageType;
        this.ID = ID;
        this.ack = ack;
        this.time = String.valueOf(LocalDateTime.now());
    }

    public DefaultSTOMPMessage(String principal, String payload, STOMPMessageType messageType, String ack, String ID) {
        this.principal = principal;
        this.payload = payload;
        this.messageType = messageType;
        this.ID = ID;
        this.ack = ack;
        this.time = String.valueOf(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "DefaultSTOMPMessage{" +
                "principal='" + principal + '\'' +
                ", payload='" + payload + '\'' +
                ", time='" + time + '\'' +
                ", messageType=" + messageType.name() +
                ", ack='" + ack + '\'' +
                ", ID='" + ID + '\'' +
                '}';
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public STOMPMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(STOMPMessageType messageType) {
        this.messageType = messageType;
    }

    public String getAck() {
        return ack;
    }

    public void setAck(String ack) {
        this.ack = ack;
    }

    public void setAckID(String ackID) {
        this.ack = ackID;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
