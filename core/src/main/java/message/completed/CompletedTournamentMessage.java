package message.completed;

import message.created.TournamentPlayMessage;

import java.util.List;

public class CompletedTournamentMessage {
    private String id;
    private List<String> winnerUsernames;

    public CompletedTournamentMessage() {
    }

    public CompletedTournamentMessage(TournamentPlayMessage tournament) {
        this.id = tournament.getTournamentID();
        this.winnerUsernames = tournament.getPlayerUsernames();
    }

    @Override
    public String toString() {
        return "CompletedTournamentMessage{" +
                "id='" + id + '\'' +
                ", winnerUsernames=" + winnerUsernames +
                '}';
    }

    public String getId() {
        return id;
    }

    public List<String> getWinnerUsernames() {
        return winnerUsernames;
    }
}
