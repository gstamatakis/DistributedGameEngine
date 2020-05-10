package message.serde;

import com.google.gson.Gson;
import message.requests.RequestCreateTournamentMessage;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class RequestCreateTournamentMessageSerde implements Serde<RequestCreateTournamentMessage>, Serializer<RequestCreateTournamentMessage>, Deserializer<RequestCreateTournamentMessage> {
    private final Gson gson = new Gson();

    @Override
    public Serializer<RequestCreateTournamentMessage> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<RequestCreateTournamentMessage> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), RequestCreateTournamentMessage.class));
    }

    @Override
    public byte[] serialize(String topic, RequestCreateTournamentMessage data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public RequestCreateTournamentMessage deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), RequestCreateTournamentMessage.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
