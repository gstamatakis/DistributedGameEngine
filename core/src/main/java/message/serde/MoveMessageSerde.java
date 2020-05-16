package message.serde;

import com.google.gson.Gson;
import message.created.MoveMessage;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class MoveMessageSerde implements Serde<MoveMessage>, Serializer<MoveMessage>, Deserializer<MoveMessage> {
    private final Gson gson = new Gson();

    @Override
    public Serializer<MoveMessage> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<MoveMessage> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), MoveMessage.class));
    }

    @Override
    public byte[] serialize(String topic, MoveMessage data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public MoveMessage deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), MoveMessage.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
