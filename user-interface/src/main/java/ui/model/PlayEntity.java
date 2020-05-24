package ui.model;

import model.GameTypeEnum;
import model.PlayTypeEnum;
import message.completed.CompletedPlayMessage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PlayEntity {

    @Id
    private String playID;
    @Column
    private String winnerPlayer;
    @Column
    private String loserPlayer;
    @Column
    private String createdBy;   //Official
    @Column
    private GameTypeEnum gameType;
    @Column
    private PlayTypeEnum playType;

    public PlayEntity() {
    }

    public PlayEntity(String playID, String winnerPlayer, String loserPlayer, String createdBy, GameTypeEnum gameType, PlayTypeEnum playType) {
        this.playID = playID;
        this.winnerPlayer = winnerPlayer;
        this.loserPlayer = loserPlayer;
        this.createdBy = createdBy;
        this.gameType = gameType;
        this.playType = playType;
    }

    public PlayEntity(CompletedPlayMessage finishedPlay) {
        this.playID = finishedPlay.getPlayID();
        this.winnerPlayer = finishedPlay.getP1();
        this.loserPlayer = finishedPlay.getP2();
        this.createdBy = finishedPlay.getCreatedBy();
        this.gameType = finishedPlay.getGameType();
        this.playType = finishedPlay.getPlayType();
    }

    public PlayEntity(String tournamentID) {
        this.playID = tournamentID;
    }

    @Override
    public String toString() {
        return "PlayEntity{" +
                "playID='" + playID + '\'' +
                ", winnerPlayer='" + winnerPlayer + '\'' +
                ", loserPlayer='" + loserPlayer + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", gameType=" + gameType +
                ", playType=" + playType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayEntity that = (PlayEntity) o;

        if (playID != null ? !playID.equals(that.playID) : that.playID != null) return false;
        if (winnerPlayer != null ? !winnerPlayer.equals(that.winnerPlayer) : that.winnerPlayer != null) return false;
        if (loserPlayer != null ? !loserPlayer.equals(that.loserPlayer) : that.loserPlayer != null) return false;
        if (createdBy != null ? !createdBy.equals(that.createdBy) : that.createdBy != null) return false;
        if (gameType != that.gameType) return false;
        return playType == that.playType;
    }

    @Override
    public int hashCode() {
        int result = playID != null ? playID.hashCode() : 0;
        result = 31 * result + (winnerPlayer != null ? winnerPlayer.hashCode() : 0);
        result = 31 * result + (loserPlayer != null ? loserPlayer.hashCode() : 0);
        result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
        result = 31 * result + (gameType != null ? gameType.hashCode() : 0);
        result = 31 * result + (playType != null ? playType.hashCode() : 0);
        return result;
    }

    public String getPlayID() {
        return playID;
    }

    public void setPlayID(String playID) {
        this.playID = playID;
    }

    public String getWinnerPlayer() {
        return winnerPlayer;
    }

    public void setWinnerPlayer(String winnerPlayer) {
        this.winnerPlayer = winnerPlayer;
    }

    public String getLoserPlayer() {
        return loserPlayer;
    }

    public void setLoserPlayer(String loserPlayer) {
        this.loserPlayer = loserPlayer;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public GameTypeEnum getGameType() {
        return gameType;
    }

    public void setGameType(GameTypeEnum gameType) {
        this.gameType = gameType;
    }

    public PlayTypeEnum getPlayType() {
        return playType;
    }

    public void setPlayType(PlayTypeEnum playType) {
        this.playType = playType;
    }
}
