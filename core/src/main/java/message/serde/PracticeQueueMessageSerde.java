package message.serde;

import com.google.gson.Gson;
import message.queue.PracticeQueueMessage;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class PracticeQueueMessageSerde implements Serde<PracticeQueueMessage>, Serializer<PracticeQueueMessage>, Deserializer<PracticeQueueMessage> {
    private final Gson gson = new Gson();

    @Override
    public Serializer<PracticeQueueMessage> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<PracticeQueueMessage> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), PracticeQueueMessage.class));
    }

    @Override
    public byte[] serialize(String topic, PracticeQueueMessage data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public PracticeQueueMessage deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), PracticeQueueMessage.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
