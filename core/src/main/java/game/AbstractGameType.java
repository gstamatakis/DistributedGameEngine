package game;

import message.completed.CompletedMoveMessage;
import message.created.MoveMessage;
import model.GameTypeEnum;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractGameType implements Serializable {
    protected Map<String, String> board; //Rows,Columns,Piece
    protected String playsFirstUsername;
    protected String playsSecondUsername;
    protected Map<Integer, MoveMessage> movesPerRoundP1;
    protected Map<Integer, MoveMessage> movesPerRoundP2;
    protected int currentRound;
    protected MoveMessage lastValidMove;
    protected GameTypeEnum gameTypeEnum;
    protected String winner;
    protected String createdBy;

    public AbstractGameType() {
    }

    protected AbstractGameType(String playsFirstUsername, String playsSecondUsername, GameTypeEnum gameType, String createdBy) {
        this.board = initialBoard();
        this.playsFirstUsername = playsFirstUsername;
        this.playsSecondUsername = playsSecondUsername;
        this.movesPerRoundP1 = new HashMap<>();
        this.movesPerRoundP2 = new HashMap<>();
        this.currentRound = 1;
        this.gameTypeEnum = gameType;
        this.winner = null;
        this.createdBy = createdBy;
    }

    //Need to override the following methods
    public abstract CompletedMoveMessage offerMove(MoveMessage message);

    public abstract Map<String, String> initialBoard();

    public abstract boolean isValidMove(MoveMessage message, Map<String, String> board);

    public abstract String emptyCell();

    //Getters and setters
    public int getCurrentRound() {
        return currentRound;
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

    public MoveMessage getLastValidMove() {
        return lastValidMove;
    }

    public GameTypeEnum getGameTypeEnum() {
        return gameTypeEnum;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getWinner() {
        return winner;
    }

    public String getLoser() {
        if (this.winner == null) {
            return null;
        }
        return this.winner.equals(getPlaysFirstUsername())
                ? this.getPlaysSecondUsername()
                : this.getPlaysFirstUsername();
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

}
