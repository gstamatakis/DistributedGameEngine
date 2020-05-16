package message.serde;

import com.google.gson.Gson;
import message.created.PlayStateMessage;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class PlayStateMessageSerde implements Serde<PlayStateMessage>, Serializer<PlayStateMessage>, Deserializer<PlayStateMessage> {
    private final Gson gson = new Gson();

    @Override
    public Serializer<PlayStateMessage> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<PlayStateMessage> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), PlayStateMessage.class));
    }

    @Override
    public byte[] serialize(String topic, PlayStateMessage data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public PlayStateMessage deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), PlayStateMessage.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
