package serde;

import com.google.gson.Gson;
import message.score.PracticePlaysScore;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class PracticePlaysScoreSerde implements Serde<PracticePlaysScore>, Serializer<PracticePlaysScore>, Deserializer<PracticePlaysScore> {
    private final Gson gson = new Gson();

    @Override
    public Serializer<PracticePlaysScore> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<PracticePlaysScore> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), PracticePlaysScore.class));
    }

    @Override
    public byte[] serialize(String topic, PracticePlaysScore data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public PracticePlaysScore deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), PracticePlaysScore.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
