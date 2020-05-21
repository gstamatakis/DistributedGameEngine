package game;

import com.google.gson.Gson;
import model.GameTypeEnum;

public class GameSerializerImpl implements GameSerializer {
    private final Gson gson = new Gson();

    @Override
    public String serializeGame(AbstractGameState abstractGameState, GameTypeEnum gameTypeEnum) {
        switch (gameTypeEnum) {
            case CHESS:
                return gson.toJson(((ChessGameState) abstractGameState), ChessGameState.class);
            case TIC_TAC_TOE:
                return gson.toJson(((TicTacToeGameState) abstractGameState), TicTacToeGameState.class);
            default:
                throw new IllegalStateException("Default case in GameSerializerImpl.serialize");
        }
    }

    @Override
    public AbstractGameState deserializeGame(String data, GameTypeEnum gameTypeEnum) {
        switch (gameTypeEnum) {
            case CHESS:
                return gson.fromJson(data, ChessGameState.class);
            case TIC_TAC_TOE:
                return gson.fromJson(data, TicTacToeGameState.class);
            default:
                throw new IllegalStateException("Default case in GameSerializerImpl.serialize");
        }
    }

    @Override
    public String newGame(GameTypeEnum gameTypeEnum, Object... args) {
        switch (gameTypeEnum) {
            case TIC_TAC_TOE:
                String p1_TTT = (String) args[0];
                String p2_TTT = (String) args[1];
                String createdBy_TTT = (String) args[2];
                TicTacToeGameState newGameTTT = new TicTacToeGameState(p1_TTT, p2_TTT, createdBy_TTT);
                return gson.toJson(newGameTTT, TicTacToeGameState.class);
            case CHESS:
                String p1 = (String) args[0];
                String p2 = (String) args[1];
                String createdBy = (String) args[2];
                ChessGameState newGame = new ChessGameState(p1, p2, createdBy);
                return gson.toJson(newGame, ChessGameState.class);
            default:
                throw new IllegalStateException("Default case in GameSerializerImpl.newGame");
        }
    }
}
