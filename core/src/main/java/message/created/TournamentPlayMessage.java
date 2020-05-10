package message.created;

import game.PlayType;
import message.DefaultPlayMessage;
import message.queue.TournamentQueueMessage;
import message.requests.RequestCreateTournamentMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TournamentPlayMessage extends DefaultPlayMessage {
    private Set<String> blacklist;
    private List<String> playerUsernames;
    private int remainingSlots;
    private String tournamentID;

    public TournamentPlayMessage(RequestCreateTournamentMessage incomingMsg) {
        super(incomingMsg.getGameType(), incomingMsg.getCreatedBy());
        this.blacklist = incomingMsg.getUsernameBlackList();
        this.playerUsernames = new ArrayList<>();
        this.remainingSlots = incomingMsg.getNumOfParticipants();
        this.tournamentID = incomingMsg.getTournamentID();
    }

    public boolean isFull() {
        return this.remainingSlots == 0;
    }

    public boolean addPlayer(TournamentQueueMessage msg) {
        if (isFull()) {
            return false;
        }
        this.playerUsernames.add(msg.getCreatedBy());
        this.remainingSlots--;
        return true;
    }

    @Override
    public PlayType playType() {
        return PlayType.TOURNAMENT;
    }

    public Set<String> getWhitelist() {
        return blacklist;
    }

    public List<String> getPlayerUsernames() {
        return playerUsernames;
    }

    public int getRemainingSlots() {
        return remainingSlots;
    }

    public String getTournamentID() {
        return tournamentID;
    }
}
