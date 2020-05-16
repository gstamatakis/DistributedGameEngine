package message.created;

public class JoinedPlayMoveMessage {
    private final MoveMessage move;
    private final PlayMessage play;

    public JoinedPlayMoveMessage(MoveMessage move, PlayMessage play) {
        this.move = move;
        this.play = play;
    }

    @Override
    public String toString() {
        return "JoinedPlayMoveMessage{" +
                "move=" + move +
                ", play=" + play +
                '}';
    }

    public MoveMessage getMove() {
        return move;
    }

    public PlayMessage getPlay() {
        return play;
    }
}
