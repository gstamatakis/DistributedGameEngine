package message.serde;

import com.google.gson.Gson;
import message.queue.TournamentQueueMessage;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class TournamentQueueMessageSerde implements Serde<TournamentQueueMessage>, Serializer<TournamentQueueMessage>, Deserializer<TournamentQueueMessage> {
    private final Gson gson = new Gson();

    @Override
    public Serializer<TournamentQueueMessage> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<TournamentQueueMessage> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), TournamentQueueMessage.class));
    }

    @Override
    public byte[] serialize(String topic, TournamentQueueMessage data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public TournamentQueueMessage deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), TournamentQueueMessage.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
