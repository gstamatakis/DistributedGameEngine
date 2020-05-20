package message.completed;

import model.GameTypeEnum;
import model.PlayTypeEnum;

public class CompletedPlayMessage {
    private final String playID;
    private final String winnerPlayer;
    private final String loserPlayer;
    private final String createdBy;
    private final GameTypeEnum gameType;
    private final PlayTypeEnum playType;

    public CompletedPlayMessage(String playID, String winnerPlayer, String loserPlayer, String createdBy, GameTypeEnum gameType, PlayTypeEnum playType) {
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

    public long getScore(String username) {
        if (username.equals(winnerPlayer)) {
            return 1;
        } else {
            return 0;
        }
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

    public GameTypeEnum getGameType() {
        return gameType;
    }

    public PlayTypeEnum getPlayType() {
        return playType;
    }
}
