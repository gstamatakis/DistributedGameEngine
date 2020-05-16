package websocket;

import java.io.Serializable;

public class InputSTOMPMessage implements Serializable {
    private String sender;
    private String payload;

    public InputSTOMPMessage() {
    }

    public InputSTOMPMessage(String sender, String payload) {
        this.sender = sender;
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sender='" + sender + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
