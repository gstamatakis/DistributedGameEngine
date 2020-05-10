package message.serde;

import com.google.gson.Gson;
import message.requests.RequestPracticeMessage;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class RequestPracticeMessageSerde implements Serde<RequestPracticeMessage>, Serializer<RequestPracticeMessage>, Deserializer<RequestPracticeMessage> {
    private final Gson gson = new Gson();

    @Override
    public Serializer<RequestPracticeMessage> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<RequestPracticeMessage> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), RequestPracticeMessage.class));
    }

    @Override
    public byte[] serialize(String topic, RequestPracticeMessage data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public RequestPracticeMessage deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), RequestPracticeMessage.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
