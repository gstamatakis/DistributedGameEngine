package serde;

import com.google.gson.Gson;
import message.score.PlayScore;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class PlayScoreSerde implements Serde<PlayScore>, Serializer<PlayScore>, Deserializer<PlayScore> {
    private final Gson gson = new Gson();

    @Override
    public Serializer<PlayScore> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<PlayScore> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), PlayScore.class));
    }

    @Override
    public byte[] serialize(String topic, PlayScore data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public PlayScore deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), PlayScore.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
