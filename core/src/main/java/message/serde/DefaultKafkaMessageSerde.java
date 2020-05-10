package message.serde;

import com.google.gson.Gson;
import message.DefaultKafkaMessage;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class DefaultKafkaMessageSerde implements Serde<DefaultKafkaMessage>, Serializer<DefaultKafkaMessage>, Deserializer<DefaultKafkaMessage> {
    private final Gson gson = new Gson();

    @Override
    public Serializer<DefaultKafkaMessage> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<DefaultKafkaMessage> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), DefaultKafkaMessage.class));
    }

    @Override
    public byte[] serialize(String topic, DefaultKafkaMessage data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public DefaultKafkaMessage deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), DefaultKafkaMessage.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
