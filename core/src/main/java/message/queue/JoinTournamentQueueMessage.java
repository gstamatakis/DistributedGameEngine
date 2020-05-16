package message.queue;

import model.PlayTypeEnum;
import message.DefaultPlayMessage;

public class JoinTournamentQueueMessage extends DefaultPlayMessage {
    private String tournamentID;

    public JoinTournamentQueueMessage(String username, String tournamentID) {
        super(null, username);
        this.tournamentID = tournamentID;
    }

    @Override
    public PlayTypeEnum playType() {
        return PlayTypeEnum.TOURNAMENT;
    }

    public String getTournamentID() {
        return tournamentID;
    }
}
