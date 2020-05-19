package message.created;

import message.DefaultPlayMessage;
import message.queue.CreateTournamentQueueMessage;
import message.queue.JoinTournamentQueueMessage;
import model.PlayTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TournamentPlayMessage extends DefaultPlayMessage {
    private String tournamentID;
    private Set<String> blacklist;
    private List<String> playerUsernames;
    private int remainingSlots;

    public TournamentPlayMessage(CreateTournamentQueueMessage incomingMsg) {
        super(incomingMsg.getGameType(), incomingMsg.getCreatedBy());
        this.blacklist = incomingMsg.getUsernameBlackList();
        this.playerUsernames = new ArrayList<>();
        this.remainingSlots = incomingMsg.getNumOfParticipants();
        this.tournamentID = incomingMsg.getTournamentID();
    }

    public boolean isFull() {
        return this.remainingSlots == 0;
    }

    public boolean addPlayer(JoinTournamentQueueMessage msg) {
        if (isFull()) {
            return false;
        }
        if (blacklist.contains(msg.getCreatedBy())) {
            return false;
        }
        this.playerUsernames.add(msg.getCreatedBy());
        this.remainingSlots--;
        return true;
    }

    @Override
    public PlayTypeEnum playType() {
        return PlayTypeEnum.TOURNAMENT;
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

    public void progressTournament() {
        this.playerUsernames.clear();
        this.remainingSlots /= 2;
    }
}
