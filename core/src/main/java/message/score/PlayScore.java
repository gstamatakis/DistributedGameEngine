package message.score;

import message.completed.CompletedPlayMessage;

import java.util.HashMap;
import java.util.Map;

public class PlayScore {
    private String playerUsername;
    private long playCount;
    private long winCount;
    private long loseCount;
    private long tieCount;
    private Map<String, Long> scorePerPlayID;
    private Map<String, String> opponentPerPlayID;
    private long totalScore;

    public PlayScore() {
    }

    public PlayScore(String playerUsername) {
        this.playerUsername = playerUsername;
        this.scorePerPlayID = new HashMap<>();
        this.opponentPerPlayID = new HashMap<>();
        this.totalScore = 0;
        this.loseCount = 0;
        this.playCount = 0;
        this.winCount = 0;
        this.tieCount = 0;
    }

    public PlayScore(String username, CompletedPlayMessage play) {
        this(username);
        this.scorePerPlayID.put(play.getPlayID(), play.getScore(username));
        this.opponentPerPlayID.put(play.getPlayID(), play.getOpponent(username));
        if (play.getWinner().isEmpty()) {
            this.tieCount++;
        } else if (play.getWinner().equals(username)) {
            this.winCount++;
        } else {
            this.loseCount++;
        }
        this.totalScore += winCount;
    }

    public PlayScore merge(PlayScore other) {
        if (!this.playerUsername.equals(other.getPlayerUsername())) {
            throw new IllegalStateException("Attempting to merge PlayScore objects with different usernames.");
        }
        PlayScore merged = new PlayScore(this.playerUsername);
        merged.winCount = this.winCount + other.winCount;
        merged.playCount = this.playCount + other.playCount;
        merged.loseCount = this.loseCount + other.loseCount;
        merged.tieCount = this.tieCount + other.tieCount;
        merged.opponentPerPlayID.putAll(this.opponentPerPlayID);
        merged.opponentPerPlayID.putAll(other.opponentPerPlayID);
        merged.scorePerPlayID.putAll(this.scorePerPlayID);
        merged.scorePerPlayID.putAll(other.scorePerPlayID);
        return merged;
    }

    @Override
    public String toString() {
        return "PlayScore{" +
                "playerUsername='" + playerUsername + '\'' +
                ", playCount=" + playCount +
                ", winCount=" + winCount +
                ", loseCount=" + loseCount +
                ", scorePerPlayID=" + scorePerPlayID +
                ", opponentPerPlayID=" + opponentPerPlayID +
                ", totalScore=" + totalScore +
                '}';
    }

    public String getPlayerUsername() {
        return playerUsername;
    }

    public Map<String, Long> getScorePerPlayID() {
        return scorePerPlayID;
    }

    public Map<String, String> getOpponentPerPlayID() {
        return opponentPerPlayID;
    }

    public long getTotalScore() {
        return totalScore;
    }

    public long getPlayCount() {
        return playCount;
    }

    public long getWinCount() {
        return winCount;
    }

    public long getLoseCount() {
        return loseCount;
    }

    public long getTieCount() {
        return tieCount;
    }
}
