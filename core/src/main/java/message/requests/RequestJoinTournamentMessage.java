package message.requests;

import model.GameTypeEnum;
import model.PlayTypeEnum;
import message.DefaultPlayMessage;

import java.util.HashSet;
import java.util.Set;

public class RequestJoinTournamentMessage extends DefaultPlayMessage {
    private String tournamentID;
    private Set<String> usernameWhitelist;
    private int numOfParticipants;

    public RequestJoinTournamentMessage(GameTypeEnum gameType, String createdBy, int numOfParticipants) {
        super(gameType, createdBy);
        this.numOfParticipants = numOfParticipants;
        this.usernameWhitelist = new HashSet<>();
    }

    public RequestJoinTournamentMessage(GameTypeEnum gameType, String createdBy, int numOfParticipants, Set<String> usernameWhitelist) {
        super(gameType, createdBy);
        this.numOfParticipants = numOfParticipants;
        this.usernameWhitelist = usernameWhitelist;
    }

    @Override
    public PlayTypeEnum playType() {
        return PlayTypeEnum.TOURNAMENT;
    }

    public String getTournamentID() {
        return tournamentID;
    }

    public Set<String> getUsernameWhitelist() {
        return usernameWhitelist;
    }

    public int getNumOfParticipants() {
        return numOfParticipants;
    }
}
