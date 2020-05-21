package message.score;

import java.util.HashMap;
import java.util.Map;

public class PracticePlaysScore {
    private String player;
    private Map<String, Long> scorePerPlayID;
    private Map<String, String> opponentPerPlayID;
    private long totalScore;

    public PracticePlaysScore() {
    }

    public PracticePlaysScore(String player) {
        this.player = player;
        this.scorePerPlayID = new HashMap<>();
        this.opponentPerPlayID = new HashMap<>();
        this.totalScore = 0;
    }

    public long addPlay(String playID, String opponent, long score) {
        this.scorePerPlayID.put(playID, score);
        this.opponentPerPlayID.put(playID, opponent);
        this.totalScore += score;
        return this.totalScore;
    }

    @Override
    public String toString() {
        return "PracticePlaysScore{" +
                "player='" + player + '\'' +
                ", scorePerPlayID=" + scorePerPlayID +
                ", opponentPerPlayID=" + opponentPerPlayID +
                ", totalScore=" + totalScore +
                '}';
    }

    public String getPlayer() {
        return player;
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
}
