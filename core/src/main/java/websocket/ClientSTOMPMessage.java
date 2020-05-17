package websocket;

import java.io.Serializable;

public class ClientSTOMPMessage implements Serializable {
    private String payload;

    public ClientSTOMPMessage(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
