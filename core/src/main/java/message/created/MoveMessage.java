package message.created;

import java.util.Objects;

public class MoveMessage {
    private String username;
    private String moveFrom;
    private String moveTo;

    public MoveMessage(String username, String moveFrom, String moveTo) {
        this.username = username;
        this.moveFrom = moveFrom;
        this.moveTo = moveTo;
    }

    @Override
    public String toString() {
        return "MoveMessage{" +
                "username='" + username + '\'' +
                ", moveFrom='" + moveFrom + '\'' +
                ", moveTo='" + moveTo + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MoveMessage that = (MoveMessage) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(moveFrom, that.moveFrom) &&
                Objects.equals(moveTo, that.moveTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, moveFrom, moveTo);
    }

    public String getUsername() {
        return username;
    }

    public String getMoveFrom() {
        return moveFrom;
    }

    public String getMoveTo() {
        return moveTo;
    }
}
