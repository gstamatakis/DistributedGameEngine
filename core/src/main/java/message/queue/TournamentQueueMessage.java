package message.queue;

import game.PlayType;
import message.DefaultPlayMessage;
import message.requests.RequestJoinTournamentMessage;

public class TournamentQueueMessage extends DefaultPlayMessage {
    private String tournamentID;
    private int numOfParticipants;

    public TournamentQueueMessage(String username, RequestJoinTournamentMessage joinMsg) {
        super(joinMsg.getGameType(), username);
        this.tournamentID = joinMsg.getTournamentID();
        this.numOfParticipants = joinMsg.getNumOfParticipants();
    }

    @Override
    public PlayType playType() {
        return PlayType.TOURNAMENT;
    }

    public String getTournamentID() {
        return tournamentID;
    }

    public int getNumOfParticipants() {
        return numOfParticipants;
    }
}
