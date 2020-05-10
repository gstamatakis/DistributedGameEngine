package message.serde;

import com.google.gson.Gson;
import message.requests.RequestJoinTournamentMessage;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class RequestJoinTournamentMessageSerde implements Serde<RequestJoinTournamentMessage>, Serializer<RequestJoinTournamentMessage>, Deserializer<RequestJoinTournamentMessage> {
    private final Gson gson = new Gson();

    @Override
    public Serializer<RequestJoinTournamentMessage> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<RequestJoinTournamentMessage> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), RequestJoinTournamentMessage.class));
    }

    @Override
    public byte[] serialize(String topic, RequestJoinTournamentMessage data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public RequestJoinTournamentMessage deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), RequestJoinTournamentMessage.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
