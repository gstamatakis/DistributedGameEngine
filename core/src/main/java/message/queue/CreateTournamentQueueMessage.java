package message.queue;

import game.GameType;
import game.PlayType;
import message.DefaultPlayMessage;

import java.util.Set;

public class CreateTournamentQueueMessage extends DefaultPlayMessage {
    private final String tournamentID;
    private Set<String> usernameBlackList;
    private int numOfParticipants;

    public CreateTournamentQueueMessage(String username, GameType gameType, Set<String> blacklist, int numOfParticipants, String tournamentID) {
        super(gameType, username);
        this.tournamentID = tournamentID;
        this.usernameBlackList = blacklist;
        this.numOfParticipants = numOfParticipants;
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
