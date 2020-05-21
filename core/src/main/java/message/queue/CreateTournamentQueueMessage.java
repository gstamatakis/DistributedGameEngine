package message.queue;

import message.DefaultPlayMessage;
import model.GameTypeEnum;
import model.PlayTypeEnum;

import java.util.Set;

public class CreateTournamentQueueMessage extends DefaultPlayMessage {
    private final String tournamentID;
    private Set<String> usernameBlackList;
    private int numOfParticipants;

    public CreateTournamentQueueMessage(String username, GameTypeEnum gameType, Set<String> blacklist, int numOfParticipants, String tournamentID) {
        super(gameType, username);
        this.tournamentID = tournamentID;
        this.usernameBlackList = blacklist;
        this.numOfParticipants = numOfParticipants;
    }

    @Override
    public String toString() {
        return super.toString() + "CreateTournamentQueueMessage{" +
                "tournamentID='" + tournamentID + '\'' +
                ", usernameBlackList=" + usernameBlackList +
                ", numOfParticipants=" + numOfParticipants +
                '}';
    }

    @Override
    public PlayTypeEnum playType() {
        return PlayTypeEnum.TOURNAMENT;
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
