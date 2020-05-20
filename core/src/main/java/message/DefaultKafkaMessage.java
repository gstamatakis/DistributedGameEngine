package message;

import com.google.gson.Gson;

import java.io.Serializable;

public class DefaultKafkaMessage implements Serializable {
    private final transient Gson gson = new Gson();
    private String payload;
    private String label;

    public DefaultKafkaMessage() {
        this.payload = "";
        this.label = "";
    }

    public DefaultKafkaMessage(Object payload, String canonicalClassName) {
        this.label = canonicalClassName;
        try {
            this.payload = gson.toJson(payload, Class.forName(canonicalClassName));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            this.payload = null;
        }
    }

    public Object retrieve(String canonicalName) {
        try {
            return this.gson.fromJson(this.payload, Class.forName(canonicalName));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isType(String canonicalName) {
        return this.label.equals(canonicalName);
    }
}
