package message.queue;

import game.PlayType;
import message.DefaultPlayMessage;

public class JoinTournamentQueueMessage extends DefaultPlayMessage {
    private String tournamentID;

    public JoinTournamentQueueMessage(String username, String tournamentID) {
        super(null, username);
        this.tournamentID = tournamentID;
    }

    @Override
    public PlayType playType() {
        return PlayType.TOURNAMENT;
    }

    public String getTournamentID() {
        return tournamentID;
    }
}
