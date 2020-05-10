package message.requests;

import game.PlayType;
import message.DefaultPlayMessage;

import java.util.Set;

public class RequestCreateTournamentMessage extends DefaultPlayMessage {
    private final String tournamentID;
    private Set<String> usernameBlackList;
    private int numOfParticipants;

    public RequestCreateTournamentMessage(String username, RequestCreateTournamentMessage reqMsg, String tournamentID) {
        super(reqMsg.getGameType(), username);
        this.tournamentID = tournamentID;
        this.usernameBlackList = reqMsg.usernameBlackList;
        this.numOfParticipants = reqMsg.numOfParticipants;
    }

    @Override
    public PlayType playType() {
        return PlayType.TOURNAMENT;
    }

    public String getTournamentID() {
        return tournamentID;
    }

    public Set<String> getUsernameBlackList() {
        return usernameBlackList;
    }

    public int getNumOfParticipants() {
        return numOfParticipants;
    }
}
