package game;

import message.completed.CompletedMoveMessage;
import message.created.MoveMessage;

import java.util.HashMap;

public class TicTacToeGame extends AbstractGameType {
    private boolean finished;

    public TicTacToeGame(String playsFirst) {
        super(playsFirst);
        this.finished = false;
        this.board = new HashMap<>(9);
        for (int i = 0; i < 9; i++) {
            board.put(String.valueOf(i), "_");
        }
    }

    @Override
    public CompletedMoveMessage offerMove(MoveMessage message) {
        return null;
    }

    @Override
    public boolean isFinished() {
        return this.finished;
    }
}
