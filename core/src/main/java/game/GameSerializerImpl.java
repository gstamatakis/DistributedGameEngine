package game;

import com.google.gson.Gson;
import model.GameTypeEnum;

public class GameSerializerImpl implements GameSerializer {
    private final Gson gson = new Gson();

    @Override
    public String serializeGame(AbstractGameType abstractGameType, GameTypeEnum gameTypeEnum) {
        switch (gameTypeEnum) {
            case CHESS:
                return gson.toJson(((ChessGameImpl) abstractGameType), ChessGameImpl.class);
            case TIC_TAC_TOE:
                return gson.toJson(((TicTacToeGameImpl) abstractGameType), TicTacToeGameImpl.class);
            default:
                throw new IllegalStateException("Default case in PlayMessage.serialize");
        }
    }

    @Override
    public AbstractGameType deserializeGame(String data, GameTypeEnum gameTypeEnum) {
        switch (gameTypeEnum) {
            case CHESS:
                return gson.fromJson(data, ChessGameImpl.class);
            case TIC_TAC_TOE:
                return gson.fromJson(data, TicTacToeGameImpl.class);
            default:
                throw new IllegalStateException("Default case in PlayMessage.serialize");
        }
    }
}
