package serde;

import com.google.gson.Gson;
import message.created.TournamentPlayMessage;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class TournamentPlayMessageSerde implements Serde<TournamentPlayMessage>, Serializer<TournamentPlayMessage>, Deserializer<TournamentPlayMessage> {
    private final Gson gson = new Gson();

    @Override
    public Serializer<TournamentPlayMessage> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<TournamentPlayMessage> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), TournamentPlayMessage.class));
    }

    @Override
    public byte[] serialize(String topic, TournamentPlayMessage data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public TournamentPlayMessage deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), TournamentPlayMessage.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
