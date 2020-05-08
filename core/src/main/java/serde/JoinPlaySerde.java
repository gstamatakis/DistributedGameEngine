package serde;

import com.google.gson.Gson;
import message.JoinPlayMessage;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class JoinPlaySerde implements Serde<JoinPlayMessage>, Serializer<JoinPlayMessage>, Deserializer<JoinPlayMessage> {
    static private Gson gson = new Gson();

    @Override
    public Serializer<JoinPlayMessage> serializer() {
        return this;
    }

    @Override
    public Deserializer<JoinPlayMessage> deserializer() {
        return this;
    }

    @Override
    public byte[] serialize(String topic, JoinPlayMessage data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public JoinPlayMessage deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), JoinPlayMessage.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
