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
    private List<String> allPlayers;
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
    public String toString() {
        return "TournamentPlayMessage{" +
                "tournamentID='" + tournamentID + '\'' +
                ", blacklist=" + blacklist +
                ", playerUsernames=" + playerUsernames +
                ", remainingSlots=" + remainingSlots +
                '}';
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

    public void setTournamentID(String tournamentID) {
        this.tournamentID = tournamentID;
    }

    public Set<String> getBlacklist() {
        return blacklist;
    }

    public void setBlacklist(Set<String> blacklist) {
        this.blacklist = blacklist;
    }

    public void setPlayerUsernames(List<String> playerUsernames) {
        this.playerUsernames = playerUsernames;
    }

    public void setRemainingSlots(int remainingSlots) {
        this.remainingSlots = remainingSlots;
    }

    public int getRemainingSlots() {
        return remainingSlots;
    }

    public String getTournamentID() {
        return tournamentID;
    }

    public List<String> getAllPlayers() {
        return allPlayers;
    }

    public void setAllPlayers(List<String> allPlayers) {
        this.allPlayers = allPlayers;
    }
}
