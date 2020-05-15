package message.created;

import game.GameType;
import game.PlayType;
import message.queue.PracticeQueueMessage;

import java.time.LocalDateTime;
import java.util.List;

public class PlayMessage {
    private String p1, p2;
    private String ID;
    private PlayType playType;
    private GameType gameType;
    private LocalDateTime createdAt;
    private int remainingRounds;

    public PlayMessage(PracticeQueueMessage msg1, PracticeQueueMessage msg2) {
        p1 = msg1.getCreatedBy();
        p2 = msg2.getCreatedBy();
        ID = generateID(msg1, msg2);
        playType = PlayType.PRACTICE;
        gameType = msg1.getGameType();
        createdAt = LocalDateTime.now();
        remainingRounds = 1;
    }

    public PlayMessage(TournamentPlayMessage message, String msg1, String msg2,int remainingRounds) {
        p1 = msg1;
        p2 = msg2;
        ID = message.getTournamentID();
        playType = PlayType.TOURNAMENT;
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

    public String getP1() {
        return p1;
    }

    public String getP2() {
        return p2;
    }

    public String getID() {
        return ID;
    }

    public PlayType getPlayType() {
        return playType;
    }

    public GameType getGameType() {
        return gameType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
