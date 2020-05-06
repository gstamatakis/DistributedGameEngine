package message;

import java.time.LocalDateTime;

public class UserJoinQueueMessage {
    private LocalDateTime timeSent;
    private String tournamentId;

    public UserJoinQueueMessage() {
        this.timeSent = java.time.LocalDateTime.now();
        this.tournamentId = "";
    }

    @Override
    public String toString() {
        return "UserJoinQueueMessage{" +
                "timeSent=" + timeSent +
                ", tournamentId='" + tournamentId + '\'' +
                '}';
    }

    public LocalDateTime getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(LocalDateTime timeSent) {
        this.timeSent = timeSent;
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }
}
