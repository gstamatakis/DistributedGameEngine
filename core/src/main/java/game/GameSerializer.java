package game;

import model.GameTypeEnum;

public interface GameSerializer {
    /**
     * Serialize a game implementation object.
     *
     * @return A serialized object in a String format.
     */
    String serializeGame(AbstractGameState abstractGameState, GameTypeEnum gameTypeEnum);

    AbstractGameState deserializeGame(String data, GameTypeEnum gameTypeEnum);

    String newGame(GameTypeEnum gameTypeEnum, Object... args);
}
