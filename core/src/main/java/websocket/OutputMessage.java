package websocket;

import java.io.Serializable;

public class OutputMessage implements Serializable {
    private String sender;
    private String payload;
    private String time;

    public OutputMessage() {
    }

    public OutputMessage(String sender, String payload, String time) {
        this.sender = sender;
        this.payload = payload;
        this.time = time;
    }

    @Override
    public String toString() {
        return "OutputMessage{" +
                "sender='" + sender + '\'' +
                ", payload='" + payload + '\'' +
                ", time='" + time + '\'' +
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
}
