package serde;

import com.google.gson.Gson;
import message.score.TournamentPlayScore;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class TournamentPlaysScoreSerde implements Serde<TournamentPlayScore>, Serializer<TournamentPlayScore>, Deserializer<TournamentPlayScore> {
    private final Gson gson = new Gson();

    @Override
    public Serializer<TournamentPlayScore> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<TournamentPlayScore> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), TournamentPlayScore.class));
    }

    @Override
    public byte[] serialize(String topic, TournamentPlayScore data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public TournamentPlayScore deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), TournamentPlayScore.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
