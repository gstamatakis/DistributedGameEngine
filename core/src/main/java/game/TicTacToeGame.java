package game;

import message.completed.CompletedMoveMessage;
import message.created.MoveMessage;

import java.util.HashMap;
import java.util.Map;

public class TicTacToeGame extends GenericGameType {
    private boolean finished;

    public TicTacToeGame(String playsFirst, String playsSecond) {
        super(playsFirst, playsSecond);
        this.finished = false;
    }

    @Override
    public CompletedMoveMessage offerMove(MoveMessage message) {
        String playedBy = message.getUsername();
        String opponent = super.getPlaysFirstUsername().equals(playedBy)
                ? super.getPlaysSecondUsername()
                : super.getPlaysFirstUsername();
        boolean valid = true;
        return new CompletedMoveMessage(valid, playedBy, opponent, message, this.finished);
    }

    @Override
    public boolean isFinished() {
        return this.finished;
    }

    @Override
    public Map<String, String> initialBoard() {
        HashMap<String, String> newBoard = new HashMap<>(9);
        for (int i = 0; i < 9; i++) {
            newBoard.put(String.valueOf(i), "_");
        }
        return newBoard;
    }

    @Override
    public boolean isValidMove(MoveMessage message, Map<String, String> board) {
        return true;
    }
}
