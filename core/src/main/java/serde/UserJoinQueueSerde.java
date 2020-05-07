package serde;

import com.google.gson.Gson;
import message.UserJoinQueueMessage;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class UserJoinQueueSerde implements Serde<UserJoinQueueMessage>, Serializer<UserJoinQueueMessage>, Deserializer<UserJoinQueueMessage> {
    static private Gson gson = new Gson();

    @Override
    public Serializer<UserJoinQueueMessage> serializer() {
        return this;
    }

    @Override
    public Deserializer<UserJoinQueueMessage> deserializer() {
        return this;
    }

    @Override
    public byte[] serialize(String topic, UserJoinQueueMessage data) {
        return gson.toJson(data).getBytes();
    }

    @Override
    public UserJoinQueueMessage deserialize(String topic, byte[] data) {
        return gson.fromJson(new String(data), UserJoinQueueMessage.class);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
