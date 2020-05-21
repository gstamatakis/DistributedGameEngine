package message;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class DefaultKafkaMessage implements Serializable {
    private static final Gson gson = new Gson();
    private static final Logger logger = LoggerFactory.getLogger(DefaultKafkaMessage.class);

    private String payload;
    private final String label;

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
            return gson.fromJson(this.payload, Class.forName(canonicalName));
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public boolean isType(String canonicalName) {
        return this.label.equals(canonicalName);
    }
}
