package serde;

import com.google.gson.Gson;
import game.AbstractGameType;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class GenericGameTypeSerde implements Serde<AbstractGameType>, Serializer<AbstractGameType>, Deserializer<AbstractGameType> {
    private final Gson gson = new Gson();

    @Override
    public Serializer<AbstractGameType> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<AbstractGameType> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), AbstractGameType.class));
    }

    @Override
    public byte[] serialize(String topic, AbstractGameType data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public AbstractGameType deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), AbstractGameType.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
