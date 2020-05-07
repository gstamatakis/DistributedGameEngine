package message;

import java.io.Serializable;
import java.time.LocalDateTime;

public class UserJoinQueueMessage implements Serializable {
    private LocalDateTime timeSent;
    private String tournamentId;
    private String username;

    public UserJoinQueueMessage() {
    }

    public UserJoinQueueMessage(String username) {
        this.timeSent = java.time.LocalDateTime.now();
        this.tournamentId = "";
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserJoinQueueMessage{" +
                "timeSent=" + timeSent +
                ", tournamentId='" + tournamentId + '\'' +
                ", username='" + username + '\'' +
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

