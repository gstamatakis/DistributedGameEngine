package serde;

import com.google.gson.Gson;
import message.PracticePlayMessage;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class PracticePlaySerde implements Serde<PracticePlayMessage>, Serializer<PracticePlayMessage>, Deserializer<PracticePlayMessage> {
    static private Gson gson = new Gson();

    @Override
    public Serializer<PracticePlayMessage> serializer() {
        return (topic, data) -> gson.toJson(data).getBytes();
    }

    @Override
    public Deserializer<PracticePlayMessage> deserializer() {
        return ((topic, data) -> gson.fromJson(new String(data), PracticePlayMessage.class));
    }

    @Override
    public byte[] serialize(String topic, PracticePlayMessage data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public PracticePlayMessage deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), PracticePlayMessage.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
