package message.serde;

import com.google.gson.Gson;
import game.GenericGameType;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class GenericGameTypeSerde implements Serde<GenericGameType>, Serializer<GenericGameType>, Deserializer<GenericGameType> {
    private final Gson gson = new Gson();

    @Override
    public Serializer<GenericGameType> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<GenericGameType> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), GenericGameType.class));
    }

    @Override
    public byte[] serialize(String topic, GenericGameType data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public GenericGameType deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), GenericGameType.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
