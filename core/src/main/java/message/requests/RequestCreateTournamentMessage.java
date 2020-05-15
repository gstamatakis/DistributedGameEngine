package message.requests;

import game.GameType;

import java.util.Set;

public class RequestCreateTournamentMessage {
    private final GameType tournamentGameType;
    private final Set<String> blackList;
    private final int numOfParticipants;
    private String tournamentID;

    public RequestCreateTournamentMessage(GameType tournamentGameType, Set<String> blackList, int numOfParticipants, String tournamentID) {
        this.tournamentGameType = tournamentGameType;
        this.blackList = blackList;
        this.numOfParticipants = numOfParticipants;
        this.tournamentID = tournamentID;
    }

    @Override
    public String toString() {
        return "CreateTournamentMessage{" +
                "tournamentGameType=" + tournamentGameType +
                ", blackList=" + blackList +
                ", numOfParticipants=" + numOfParticipants +
                ", tournamentID='" + tournamentID + '\'' +
                '}';
    }

    public GameType getTournamentGameType() {
        return tournamentGameType;
    }

    public Set<String> getBlackList() {
        return blackList;
    }

    public int getNumOfParticipants() {
        return numOfParticipants;
    }

    public String getTournamentID() {
        return tournamentID;
    }
}
