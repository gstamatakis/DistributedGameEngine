package game;

import message.completed.CompletedMoveMessage;
import message.created.MoveMessage;

import java.util.HashMap;
import java.util.Map;

public class GenericGameType {
    protected Map<String, String> board; //Rows,Columns,Piece
    protected String playsFirstUsername;
    protected String playsSecondUsername;
    protected Map<Integer, MoveMessage> movesPerRoundP1;
    protected Map<Integer, MoveMessage> movesPerRoundP2;

    public GenericGameType(String playsFirstUsername, String playsSecondUsername) {
        this.board = initialBoard();
        this.playsFirstUsername = playsFirstUsername;
        this.playsSecondUsername = playsSecondUsername;
        this.movesPerRoundP1 = new HashMap<>();
        this.movesPerRoundP2 = new HashMap<>();
    }

    //Need to override the following methods
    public CompletedMoveMessage offerMove(MoveMessage message) {
        throw new UnsupportedOperationException();
    }

    public boolean isFinished() {
        throw new UnsupportedOperationException();
    }

    public Map<String, String> initialBoard() {
        throw new UnsupportedOperationException();
    }

    public boolean isValidMove(MoveMessage message, Map<String, String> board) {
        throw new UnsupportedOperationException();
    }

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

    public String getPlaysSecondUsername() {
        return playsSecondUsername;
    }
}
