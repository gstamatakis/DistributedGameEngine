package game;

import message.completed.CompletedMoveMessage;
import message.created.MoveMessage;

import java.util.Map;

public abstract class AbstractGameType {
    protected Map<String, String> board; //Rows,Columns,Piece
    protected String playsFirstUsername;
    protected Map<Integer, MoveMessage> movesPerRoundP1;
    protected Map<Integer, MoveMessage> movesPerRoundP2;

    public AbstractGameType(String playsFirstUsername) {
        this.playsFirstUsername = playsFirstUsername;
    }

    public abstract CompletedMoveMessage offerMove(MoveMessage message);

    public abstract boolean isFinished();

    public Map<String, String> getBoard() {
        return board;
    }

    public String getPlaysFirstUsername() {
        return playsFirstUsername;
    }

    public Map<Integer, MoveMessage> getMovesPerRoundP1() {
        return movesPerRoundP1;
    }

    public Map<Integer, MoveMessage> getMovesPerRoundP2() {
        return movesPerRoundP2;
    }
}
