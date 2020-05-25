package game;

import message.completed.CompletedMoveMessage;
import message.created.MoveMessage;
import model.GameTypeEnum;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractGameState implements Serializable {
    protected Map<String, String> board; //Rows,Columns,Piece
    protected String playsFirstUsername;
    protected String playsSecondUsername;
    protected Map<Integer, MoveMessage> movesPerRoundP1;
    protected Map<Integer, MoveMessage> movesPerRoundP2;
    protected int currentRound;
    protected MoveMessage lastValidMoveMessage;
    protected GameTypeEnum gameTypeEnum;
    protected int winner;
    protected String createdBy;
    protected boolean finished;

    public AbstractGameState() {
    }

    protected AbstractGameState(String playsFirstUsername, String playsSecondUsername, GameTypeEnum gameType, String createdBy) {
        this.board = initialBoard();
        this.playsFirstUsername = playsFirstUsername;
        this.playsSecondUsername = playsSecondUsername;
        this.movesPerRoundP1 = new HashMap<>();
        this.movesPerRoundP2 = new HashMap<>();
        this.currentRound = 1;
        this.gameTypeEnum = gameType;
        this.winner = 0;    //TIE
        this.finished = false;
        this.createdBy = createdBy;
    }

    //Need to override the following methods
    public abstract CompletedMoveMessage offerMove(MoveMessage message);

    public abstract Map<String, String> initialBoard();

    public abstract boolean isValidMove(MoveMessage message, Map<String, String> board);

    public abstract String emptyCell();

    public String getPrintableBoard() {
        return this.board.toString();
    }

    @Override
    public String toString() {
        return "AbstractGameState{" +
                "board=" + board +
                ", playsFirstUsername='" + playsFirstUsername + '\'' +
                ", playsSecondUsername='" + playsSecondUsername + '\'' +
                ", movesPerRoundP1=" + movesPerRoundP1 +
                ", movesPerRoundP2=" + movesPerRoundP2 +
                ", currentRound=" + currentRound +
                ", lastValidMove=" + lastValidMoveMessage +
                ", gameTypeEnum=" + gameTypeEnum +
                ", winner='" + winner + '\'' +
                ", createdBy='" + createdBy + '\'' +
                '}';
    }

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

    public MoveMessage getLastValidMoveMessage() {
        return lastValidMoveMessage;
    }

    public GameTypeEnum getGameTypeEnum() {
        return gameTypeEnum;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public int getWinner() {
        return winner;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setWinner(int winner) {
        this.winner = winner;
    }
}
