package message.completed;

import message.created.TournamentPlayMessage;

import java.util.List;

public class CompletedTournamentMessage {
    private String id;
    private List<String> winnerUsernames;
    private List<String> allPlayers;

    public CompletedTournamentMessage() {
    }

    public CompletedTournamentMessage(TournamentPlayMessage tournament) {
        this.id = tournament.getTournamentID();
        this.winnerUsernames = tournament.getPlayerUsernames();
        this.allPlayers = tournament.getAllPlayers();
    }

    @Override
    public String toString() {
        return "CompletedTournamentMessage{" +
                "id='" + id + '\'' +
                ", winnerUsernames=" + winnerUsernames +
                ", allPlayers=" + allPlayers +
                '}';
    }

    public String getId() {
        return id;
    }

    public List<String> getWinnerUsernames() {
        return winnerUsernames;
    }

    public List<String> getAllPlayers() {
        return allPlayers;
    }
}
