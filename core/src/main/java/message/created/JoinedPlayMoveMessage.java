package message.created;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoinedPlayMoveMessage {
    private static final Logger logger = LoggerFactory.getLogger(JoinedPlayMoveMessage.class);
    private final MoveMessage move;
    private final PlayMessage play;

    public JoinedPlayMoveMessage(MoveMessage move, PlayMessage play) {
        this.move = move;
        this.play = play;
    }

    @Override
    public String toString() {
        return "JoinedPlayMoveMessage{" +
                "move=" + (move != null ? move.toString() : null) +
                ", play=" + (play != null ? play.toString() : null) +
                '}';
    }

    public MoveMessage getMove() {
        return move;
    }

    public PlayMessage getPlay() {
        return play;
    }
}
