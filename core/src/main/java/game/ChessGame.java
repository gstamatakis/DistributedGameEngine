package game;

import message.completed.CompletedMoveMessage;
import message.created.MoveMessage;

import java.util.HashMap;

public class ChessGame extends AbstractGameType {
    private boolean finished;

    public ChessGame(String playsFirst) {
        super(playsFirst);
        this.finished = false;
        this.board = new HashMap<>();
    }

    @Override
    public CompletedMoveMessage offerMove(MoveMessage message) {
        return null;
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
