package websocket;

import java.io.Serializable;

public class ClientSTOMPMessage implements Serializable {
    private String payload;
    private String ID;

    public ClientSTOMPMessage() {
    }

    public ClientSTOMPMessage(String payload, String id) {
        this.payload = payload;
        this.ID = id;
    }

    public ClientSTOMPMessage(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "ClientSTOMPMessage{" +
                "payload='" + payload + '\'' +
                ", ID='" + ID + '\'' +
                '}';
    }

    public String getPayload() {
        return payload;
    }

    public String getID() {
        return ID;
    }
}
