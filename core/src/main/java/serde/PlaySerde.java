package serde;

import com.google.gson.Gson;
import message.PlayMessage;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class PlaySerde implements Serde<PlayMessage>, Serializer<PlayMessage>, Deserializer<PlayMessage> {
    static private Gson gson = new Gson();

    @Override
    public Serializer<PlayMessage> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<PlayMessage> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), PlayMessage.class));
    }

    @Override
    public byte[] serialize(String topic, PlayMessage data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public PlayMessage deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), PlayMessage.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
