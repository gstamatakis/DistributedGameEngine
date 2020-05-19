package message.created;


import game.AbstractGameType;
import game.ChessGameImpl;
import game.TicTacToeGameImpl;
import message.queue.PracticeQueueMessage;
import model.GameTypeEnum;
import model.PlayTypeEnum;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PlayMessage implements Serializable {
    private String p1, p2;
    private String ID;
    private PlayTypeEnum playTypeEnum;
    private GameTypeEnum gameTypeEnum;
    private String createdAt;
    private int remainingRounds;
    private AbstractGameType abstractGameType;

    public PlayMessage() {
    }

    public PlayMessage(PracticeQueueMessage msg1, PracticeQueueMessage msg2) {
        p1 = msg1.getCreatedBy();
        p2 = msg2.getCreatedBy();
        ID = generateID(msg1, msg2);
        playTypeEnum = PlayTypeEnum.PRACTICE;
        gameTypeEnum = msg1.getGameType();
        createdAt = String.valueOf(LocalDateTime.now());
        remainingRounds = 1;
        initGameType();
    }

    public PlayMessage(TournamentPlayMessage message, String msg1, String msg2, int remainingRounds) {
        p1 = msg1;
        p2 = msg2;
        ID = message.getTournamentID();
        playTypeEnum = PlayTypeEnum.TOURNAMENT;
        gameTypeEnum = message.getGameType();
        createdAt = String.valueOf(LocalDateTime.now());
        this.remainingRounds = remainingRounds;
        initGameType();
    }

    public PlayMessage(String p1, String p2, String ID, PlayTypeEnum playTypeEnum, GameTypeEnum gameTypeEnum, String createdAt, int remainingRounds, AbstractGameType abstractGameType) {
        this.p1 = p1;
        this.p2 = p2;
        this.ID = ID;
        this.playTypeEnum = playTypeEnum;
        this.gameTypeEnum = gameTypeEnum;
        this.createdAt = createdAt;
        this.remainingRounds = remainingRounds;
        this.abstractGameType = abstractGameType;
    }

    void initGameType() {
        switch (this.gameTypeEnum) {
            case TIC_TAC_TOE:
                abstractGameType = new TicTacToeGameImpl(p1, p2);
                break;
            case CHESS:
                abstractGameType = new ChessGameImpl(p1, p2);
                break;
            default:
                throw new IllegalStateException("Default case in PlayMessage 2nd constructor!");
        }
    }

    private String generateID(PracticeQueueMessage msg1, PracticeQueueMessage msg2) {
        return System.nanoTime() + "_" + msg1.getCreatedBy().hashCode();
    }

    @Override
    public String toString() {
        return "PlayMessage{" +
                "p1='" + p1 + '\'' +
                ", p2='" + p2 + '\'' +
                ", ID='" + ID + '\'' +
                ", playTypeEnum=" + playTypeEnum +
                ", gameTypeEnum=" + gameTypeEnum +
                ", createdAt=" + createdAt +
                ", remainingRounds=" + remainingRounds +
                ", gameType=" + abstractGameType.toString() +
                '}';
    }

    public String getOpponent(String username) {
        return this.p1.equals(username) ? p2 : p1;
    }

    public String getP1() {
        return p1;
    }

    public String getP2() {
        return p2;
    }

    public String getID() {
        return ID;
    }

    public PlayTypeEnum getPlayTypeEnum() {
        return playTypeEnum;
    }

    public GameTypeEnum getGameTypeEnum() {
        return gameTypeEnum;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getRemainingRounds() {
        return remainingRounds;
    }

    public AbstractGameType getAbstractGameType() {
        return abstractGameType;
    }
}
