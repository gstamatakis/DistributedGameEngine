package game;

import model.GameTypeEnum;

public interface GameSerializer {
    /**
     * Serialize a game implementation object.
     *
     * @return A serialized object in a String format.
     */
    String serializeGame(AbstractGameType abstractGameType, GameTypeEnum gameTypeEnum);

    AbstractGameType deserializeGame(String data, GameTypeEnum gameTypeEnum);
}
