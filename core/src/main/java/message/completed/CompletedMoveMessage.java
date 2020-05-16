package message.completed;

import message.created.MoveMessage;

public class CompletedMoveMessage {
    private boolean valid;
    private String playedByUsername;
    private String opponentUsername;
    private MoveMessage moveMessage;
    private boolean playFinished;

    public CompletedMoveMessage(boolean valid, String playedByUsername, String opponentUsername, MoveMessage moveMessage,boolean playFinished) {
        this.valid = valid;
        this.playedByUsername = playedByUsername;
        this.opponentUsername = opponentUsername;
        this.moveMessage = moveMessage;
        this.playFinished = playFinished;
    }

    public boolean isValid() {
        return valid;
    }

    public String getPlayedByUsername() {
        return playedByUsername;
    }

    public String getOpponentUsername() {
        return opponentUsername;
    }

    public MoveMessage getMoveMessage() {
        return moveMessage;
    }
}
