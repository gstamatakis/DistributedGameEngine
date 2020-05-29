package message.completed;

import model.GameTypeEnum;
import model.PlayTypeEnum;

public class CompletedPlayMessage {
    private final String playID;
    private final String p1;
    private final String p2;
    private final int winner;   // -1 for p1 , 0 for a tie ,+1 for p2
    private final String createdBy;
    private final GameTypeEnum gameType;
    private final PlayTypeEnum playType;

    public CompletedPlayMessage(String playID, String p1, String p2, int winner, String createdBy, GameTypeEnum gameType, PlayTypeEnum playType) {
        this.playID = playID;
        this.p1 = p1;
        this.p2 = p2;
        this.winner = winner;
        this.createdBy = createdBy;
        this.gameType = gameType;
        this.playType = playType;
    }

    @Override
    public String toString() {
        return "CompletedPlayMessage{" +
                "playID='" + playID + '\'' +
                ", p1='" + p1 + '\'' +
                ", p2='" + p2 + '\'' +
                ", winner=" + getWinner() + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", gameType=" + gameType +
                ", playType=" + playType +
                '}';
    }

    public boolean isTie() {
        return this.getWinner().isEmpty();
    }

    public String getWinner() {
        if (winner == -1) {
            return p1;
        } else if (winner == +1) {
            return p2;
        } else {
            return "";
        }
    }

    public String getLoser() {
        if (winner == -1) {
            return p2;
        } else if (winner == +1) {
            return p1;
        } else {
            return "";
        }
    }

    /**
     * Calculate the score of a player.
     *
     * @param username The player's username.
     * @return Their score on this play.
     */
    public long getScore(String username) {
        if (username.equals(getWinner())) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Returne the opponent of the player.
     *
     * @param username The player's username.
     * @return The username of the opponent.
     */
    public String getOpponent(String username) {
        if (this.getP1().equals(username)) {
            return p2;
        } else if (this.getP2().equals(username)) {
            return p1;
        } else {
            throw new IllegalStateException(String.format("CompletedPlayMessage.getOpponent invalid input [%s}.", username));
        }
    }

    public String getPlayID() {
        return playID;
    }

    public String getP1() {
        return p1;
    }

    public String getP2() {
        return p2;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public GameTypeEnum getGameType() {
        return gameType;
    }

    public PlayTypeEnum getPlayType() {
        return playType;
    }
}
