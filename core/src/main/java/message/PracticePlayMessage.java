package message;

import java.time.LocalDateTime;

public class PracticePlayMessage {
    private String playID;
    private UserJoinQueueMessage player1;
    private UserJoinQueueMessage player2;
    private LocalDateTime createdAt;

    public PracticePlayMessage(UserJoinQueueMessage msg1, UserJoinQueueMessage msg2) {
        this.player1 = msg1;
        this.player2 = msg2;
        this.createdAt = java.time.LocalDateTime.now();
        this.playID = String.valueOf(hashCode());
    }

    @Override
    public String toString() {
        return "PracticePlayMessage{" +
                "playID='" + playID + '\'' +
                ", player1=" + player1 +
                ", player2=" + player2 +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PracticePlayMessage that = (PracticePlayMessage) o;

        if (!player1.equals(that.player1)) return false;
        if (!player2.equals(that.player2)) return false;
        return createdAt.equals(that.createdAt);
    }

    @Override
    public int hashCode() {
        int result = player1.hashCode();
        result = 31 * result + player2.hashCode();
        result = 31 * result + createdAt.hashCode();
        return result;
    }

    public UserJoinQueueMessage getPlayer1() {
        return player1;
    }

    public UserJoinQueueMessage getPlayer2() {
        return player2;
    }

    public String getPlayID() {
        return playID;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
