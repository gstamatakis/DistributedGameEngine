package message.queue;

import message.DefaultPlayMessage;
import model.PlayTypeEnum;

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
