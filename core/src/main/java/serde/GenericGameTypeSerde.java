package serde;

import com.google.gson.Gson;
import game.AbstractGameState;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class GenericGameTypeSerde implements Serde<AbstractGameState>, Serializer<AbstractGameState>, Deserializer<AbstractGameState> {
    private final Gson gson = new Gson();

    @Override
    public Serializer<AbstractGameState> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<AbstractGameState> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), AbstractGameState.class));
    }

    @Override
    public byte[] serialize(String topic, AbstractGameState data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public AbstractGameState deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), AbstractGameState.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
