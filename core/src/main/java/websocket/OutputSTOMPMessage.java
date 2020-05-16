package websocket;

import java.io.Serializable;
import java.security.Principal;

public class OutputSTOMPMessage implements Serializable {
    private Principal principal;
    private String sender;
    private String payload;
    private String time;

    public OutputSTOMPMessage() {
    }

    public OutputSTOMPMessage(String sender, String payload, String time) {
        this.sender = sender;
        this.payload = payload;
        this.time = time;
        this.principal = null;
    }


    public OutputSTOMPMessage(Principal principal, String sender, String payload, String time) {
        this.principal = principal;
        this.sender = sender;
        this.payload = payload;
        this.time = time;
    }

    @Override
    public String toString() {
        return "OutputMessage{" +
                "sender='" + sender + '\'' +
                ", principal='" + (principal != null ? principal.toString() : "null") + '\'' +
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
