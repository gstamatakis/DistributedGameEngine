package serde;

import com.google.gson.Gson;
import message.created.JoinedPlayMoveMessage;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class JoinedPlayMoveMessageSerde implements Serde<JoinedPlayMoveMessage>, Serializer<JoinedPlayMoveMessage>, Deserializer<JoinedPlayMoveMessage> {
    private final Gson gson = new Gson();

    @Override
    public Serializer<JoinedPlayMoveMessage> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<JoinedPlayMoveMessage> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), JoinedPlayMoveMessage.class));
    }

    @Override
    public byte[] serialize(String topic, JoinedPlayMoveMessage data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public JoinedPlayMoveMessage deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), JoinedPlayMoveMessage.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
