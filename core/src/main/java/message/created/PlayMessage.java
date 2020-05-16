package message.created;


import message.queue.PracticeQueueMessage;
import model.GameTypeEnum;
import model.PlayTypeEnum;

import java.time.LocalDateTime;

public class PlayMessage {
    private String p1, p2;
    private String ID;
    private PlayTypeEnum playType;
    private GameTypeEnum gameType;
    private LocalDateTime createdAt;
    private int remainingRounds;

    public PlayMessage(PracticeQueueMessage msg1, PracticeQueueMessage msg2) {
        p1 = msg1.getCreatedBy();
        p2 = msg2.getCreatedBy();
        ID = generateID(msg1, msg2);
        playType = PlayTypeEnum.PRACTICE;
        gameType = msg1.getGameType();
        createdAt = LocalDateTime.now();
        remainingRounds = 1;
    }

    public PlayMessage(TournamentPlayMessage message, String msg1, String msg2, int remainingRounds) {
        p1 = msg1;
        p2 = msg2;
        ID = message.getTournamentID();
        playType = PlayTypeEnum.TOURNAMENT;
        gameType = message.getGameType();
        createdAt = LocalDateTime.now();
        this.remainingRounds = remainingRounds;
    }

    private String generateID(PracticeQueueMessage msg1, PracticeQueueMessage msg2) {
        return System.nanoTime() + "_" + msg1.getCreatedBy().hashCode();
    }

    @Override
    public String toString() {
        return "PlayMessage{" +
                "playerUsernames=" + p1 + ',' + p2 +
                ", ID='" + ID + '\'' +
                ", playType=" + playType +
                ", gameType=" + gameType +
                ", createdAt=" + createdAt +
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

    public PlayTypeEnum getPlayType() {
        return playType;
    }

    public GameTypeEnum getGameType() {
        return gameType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getRemainingRounds() {
        return remainingRounds;
    }
}
