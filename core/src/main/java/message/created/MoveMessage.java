package message.created;

public class MoveMessage {
    private String username;
    private String move;
    private String playID;

    public MoveMessage(String username, String move, String playID) {
        this.username = username;
        this.move = move;
        this.playID = playID;
    }

    @Override
    public String toString() {
        return "MoveMessage{" +
                "username='" + username + '\'' +
                ", move='" + move + '\'' +
                ", playID='" + playID + '\'' +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public String getMove() {
        return move;
    }

    public String getPlayID() {
        return playID;
    }
}
