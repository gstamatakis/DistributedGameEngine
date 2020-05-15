package message.serde;

import com.google.gson.Gson;
import message.queue.CreateTournamentQueueMessage;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class CreateTournamentQueueMessageSerde implements Serde<CreateTournamentQueueMessage>, Serializer<CreateTournamentQueueMessage>, Deserializer<CreateTournamentQueueMessage> {
    private final Gson gson = new Gson();

    @Override
    public Serializer<CreateTournamentQueueMessage> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<CreateTournamentQueueMessage> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), CreateTournamentQueueMessage.class));
    }

    @Override
    public byte[] serialize(String topic, CreateTournamentQueueMessage data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public CreateTournamentQueueMessage deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), CreateTournamentQueueMessage.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
