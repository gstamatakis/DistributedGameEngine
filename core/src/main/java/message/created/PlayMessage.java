package message.created;


import game.AbstractGameState;
import game.GameSerializer;
import game.GameSerializerImpl;
import message.queue.PracticeQueueMessage;
import model.GameTypeEnum;
import model.PlayTypeEnum;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PlayMessage implements Serializable {
    private static final GameSerializer gameSerializer = new GameSerializerImpl();

    private String p1, p2;
    private String ID;
    private PlayTypeEnum playTypeEnum;
    private GameTypeEnum gameTypeEnum;
    private String createdAt;
    private int remainingRounds;
    private String gameState;
    private String lastUserWhoMoved;

    public PlayMessage() {
    }

    public PlayMessage(PracticeQueueMessage msg1, PracticeQueueMessage msg2, Object... gameStateArgs) {
        this.p1 = msg1.getCreatedBy();
        this.p2 = msg2.getCreatedBy();
        this.ID = generateID(msg1, msg2);
        this.playTypeEnum = PlayTypeEnum.PRACTICE;
        this.gameTypeEnum = msg1.getGameType();
        this.createdAt = String.valueOf(LocalDateTime.now());
        this.remainingRounds = 1;
        this.gameState = gameSerializer.newGame(gameTypeEnum, gameStateArgs);
        this.lastUserWhoMoved = msg1.getCreatedBy();
    }

    public PlayMessage(TournamentPlayMessage tournamentMsg, String p1, String p2, int remainingRounds, Object... gameStateArgs) {
        this.p1 = p1;
        this.p2 = p2;
        this.ID = generateID(tournamentMsg, p1, p2);
        this.playTypeEnum = PlayTypeEnum.TOURNAMENT;
        this.gameTypeEnum = tournamentMsg.getGameType();
        this.createdAt = String.valueOf(LocalDateTime.now());
        this.remainingRounds = remainingRounds;
        this.gameState = gameSerializer.newGame(gameTypeEnum, gameStateArgs);
        this.lastUserWhoMoved = tournamentMsg.getCreatedBy();
    }

    private String generateID(PracticeQueueMessage msg1, PracticeQueueMessage msg2) {
        return System.nanoTime() + "_" + msg1.getCreatedBy().hashCode();
    }

    private String generateID(TournamentPlayMessage tournamentMsg, String p1, String p2) {
        return String.format("%s_%s", System.nanoTime(), (tournamentMsg.getTournamentID() + p1 + p2).hashCode());
    }

    public String getNeedsToWait() {
        return this.lastUserWhoMoved;
    }

    public String getNeedsToMove() {
        return this.lastUserWhoMoved.equals(this.p1) ? this.p2 : this.p1;
    }

    public AbstractGameState getGameState() {
        return gameSerializer.deserializeGame(gameState, gameTypeEnum);
    }

    public void setGameState(AbstractGameState gameState) {
        this.gameState = gameSerializer.serializeGame(gameState, this.gameTypeEnum);
    }

    @Override
    public String toString() {
        return "PlayMessage{" +
                "p1='" + p1 + '\'' +
                ", p2='" + p2 + '\'' +
                ", ID='" + ID + '\'' +
                ", playTypeEnum=" + playTypeEnum +
                ", gameTypeEnum=" + gameTypeEnum +
                ", createdAt='" + createdAt + '\'' +
                ", remainingRounds=" + remainingRounds +
                ", gameState='" + getGameState().toString() + '\'' +
                ", lastUserWhoMoved='" + lastUserWhoMoved + '\'' +
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

    public String getLastUserWhoMoved() {
        return lastUserWhoMoved;
    }

    public void setLastUserWhoMoved(String username) {
        this.lastUserWhoMoved = username;
    }
}
