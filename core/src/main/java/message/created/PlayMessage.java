package message.created;

import game.GameType;
import game.PlayType;
import message.queue.PracticeQueueMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PlayMessage {
    private List<String> playerUsernames;
    private String ID;
    private PlayType playType;
    private GameType gameType;
    private LocalDateTime createdAt;

    public PlayMessage(PracticeQueueMessage msg1, PracticeQueueMessage msg2) {
        playerUsernames = new ArrayList<>();
        playerUsernames.add(msg1.getCreatedBy());
        playerUsernames.add(msg2.getCreatedBy());
        ID = generateID(msg1, msg2);
        playType = PlayType.PRACTICE;
        gameType = msg1.getGameType();
        createdAt = LocalDateTime.now();
    }

    public PlayMessage(TournamentPlayMessage message) {
        playerUsernames = message.getPlayerUsernames();
        ID = message.getTournamentID();
        playType = PlayType.TOURNAMENT;
        gameType = message.getGameType();
        createdAt = LocalDateTime.now();
    }

    private String generateID(PracticeQueueMessage msg1, PracticeQueueMessage msg2) {
        return System.nanoTime() + "_" + msg1.getCreatedBy().hashCode();
    }

    @Override
    public String toString() {
        return "PlayMessage{" +
                "playerUsernames=" + playerUsernames +
                ", ID='" + ID + '\'' +
                ", playType=" + playType +
                ", gameType=" + gameType +
                ", createdAt=" + createdAt +
                '}';
    }

    public List<String> getPlayerUsernames() {
        return playerUsernames;
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
