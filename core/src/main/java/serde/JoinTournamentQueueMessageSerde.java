package serde;

import com.google.gson.Gson;
import message.queue.JoinTournamentQueueMessage;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class JoinTournamentQueueMessageSerde implements Serde<JoinTournamentQueueMessage>, Serializer<JoinTournamentQueueMessage>, Deserializer<JoinTournamentQueueMessage> {
    private final Gson gson = new Gson();

    @Override
    public Serializer<JoinTournamentQueueMessage> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<JoinTournamentQueueMessage> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), JoinTournamentQueueMessage.class));
    }

    @Override
    public byte[] serialize(String topic, JoinTournamentQueueMessage data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public JoinTournamentQueueMessage deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), JoinTournamentQueueMessage.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
