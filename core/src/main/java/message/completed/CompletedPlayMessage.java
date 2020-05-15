package message.completed;

import game.GameType;
import game.PlayType;

public class CompletedPlayMessage {
    private final String playID;
    private final String winnerPlayer;
    private final String loserPlayer;
    private final String createdBy;
    private final GameType gameType;
    private final PlayType playType;

    public CompletedPlayMessage(String playID, String winnerPlayer, String loserPlayer, String createdBy, GameType gameType, PlayType playType) {
        this.playID = playID;
        this.winnerPlayer = winnerPlayer;
        this.loserPlayer = loserPlayer;
        this.createdBy = createdBy;
        this.gameType = gameType;
        this.playType = playType;
    }

    @Override
    public String toString() {
        return "CompletedPlayMessage{" +
                "playID='" + playID + '\'' +
                ", winnerPlayer='" + winnerPlayer + '\'' +
                ", loserPlayer='" + loserPlayer + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", gameType=" + gameType +
                ", playType=" + playType +
                '}';
    }

    public String getPlayID() {
        return playID;
    }

    public String getWinnerPlayer() {
        return winnerPlayer;
    }

    public String getLoserPlayer() {
        return loserPlayer;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public GameType getGameType() {
        return gameType;
    }

    public PlayType getPlayType() {
        return playType;
    }
}
