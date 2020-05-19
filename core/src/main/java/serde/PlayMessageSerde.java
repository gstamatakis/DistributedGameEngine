package serde;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import game.AbstractGameType;
import game.GameSerializer;
import game.GameSerializerImpl;
import message.created.PlayMessage;
import model.GameTypeEnum;
import model.PlayTypeEnum;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class PlayMessageSerde implements Serde<PlayMessage>, Serializer<PlayMessage>, Deserializer<PlayMessage> {
    private final Gson gson = new Gson();
    private final GameSerializer gameSerializer = new GameSerializerImpl();

    @Override
    public Serializer<PlayMessage> serializer() {
        return (topic, data) -> mySerializer(data).getBytes();
    }

    @Override
    public Deserializer<PlayMessage> deserializer() {
        return ((topic, data) -> myDeserializer(new String(data)));
    }

    @Override
    public byte[] serialize(String topic, PlayMessage data) {
        return mySerializer(data).getBytes();
    }

    @Override
    public PlayMessage deserialize(String topic, byte[] data) {
        return myDeserializer(new String(data));
    }

    private String mySerializer(PlayMessage data) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("p1", data.getP1());
        jsonObject.addProperty("p2", data.getP2());
        jsonObject.addProperty("ID", data.getID());
        jsonObject.addProperty("playTypeEnum", data.getPlayTypeEnum().toString());
        jsonObject.addProperty("gameTypeEnum", data.getGameTypeEnum().toString());
        jsonObject.addProperty("createdAt", data.getCreatedAt());
        jsonObject.addProperty("remainingRounds", data.getRemainingRounds());
        jsonObject.addProperty("state", gameSerializer.serializeGame(data.getAbstractGameType(), data.getGameTypeEnum()));
        return gson.toJson(jsonObject, JsonObject.class);
    }

    private PlayMessage myDeserializer(String data) {
        JsonObject jsonObject = gson.fromJson(data, JsonObject.class);
        String p1 = jsonObject.get("p1").getAsString();
        String p2 = jsonObject.get("p2").getAsString();
        String id = jsonObject.get("ID").getAsString();
        PlayTypeEnum playTypeEnum = PlayTypeEnum.valueOf(jsonObject.get("playTypeEnum").getAsString());
        GameTypeEnum gameTypeEnum = GameTypeEnum.valueOf(jsonObject.get("gameTypeEnum").getAsString());
        String createdAt = jsonObject.get("createdAt").getAsString();
        int remainingRounds = jsonObject.get("remainingRounds").getAsInt();
        String state = jsonObject.get("state").getAsString();
        AbstractGameType abstractGameType = gameSerializer.deserializeGame(state, gameTypeEnum);
        return new PlayMessage(p1, p2, id, playTypeEnum, gameTypeEnum, createdAt, remainingRounds, abstractGameType);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }
}
