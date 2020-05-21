package message.created;

import message.DefaultKafkaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoinedPlayMoveMessage {
    private static final Logger logger = LoggerFactory.getLogger(JoinedPlayMoveMessage.class);
    private final MoveMessage move;
    private final PlayMessage play;

    public JoinedPlayMoveMessage(DefaultKafkaMessage move, DefaultKafkaMessage play) {
        this.move = (MoveMessage) move.retrieve(MoveMessage.class.getCanonicalName());
        this.play = (PlayMessage) play.retrieve(PlayMessage.class.getCanonicalName());
        logger.info(String.format("Joining [%s] and [%s].", move, play));
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
