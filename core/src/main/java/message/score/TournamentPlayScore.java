package message.score;

import java.util.Map;

public class TournamentPlayScore {
    private String tournamentID;
    private Map<String, Long> playerScores;

    public TournamentPlayScore(String tournamentID, Map<String, Long> playerScores) {
        this.tournamentID = tournamentID;
        this.playerScores = playerScores;
    }

    @Override
    public String toString() {
        return "TournamentPlayScore{" +
                "tournamentID='" + tournamentID + '\'' +
                ", playerScores=" + playerScores +
                '}';
    }

    public String getTournamentID() {
        return tournamentID;
    }

    public Map<String, Long> getPlayerScores() {
        return playerScores;
    }
}
