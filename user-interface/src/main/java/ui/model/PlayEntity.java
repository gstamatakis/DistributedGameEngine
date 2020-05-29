package ui.model;

import message.completed.CompletedPlayMessage;
import message.completed.CompletedTournamentMessage;
import message.created.PlayMessage;
import message.requests.RequestCreateTournamentMessage;
import model.GameTypeEnum;
import model.PlayTypeEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
    @Column
    private String spectatorList;

    public PlayEntity() {
    }

    public PlayEntity(CompletedPlayMessage finishedPlay) {
        this.playID = finishedPlay.getPlayID();
        this.winnerPlayer = finishedPlay.getP1();
        this.loserPlayer = finishedPlay.getP2();
        this.createdBy = finishedPlay.getCreatedBy();
        this.gameType = finishedPlay.getGameType();
        this.playType = finishedPlay.getPlayType();
        this.spectatorList = "";
    }

    public PlayEntity(PlayMessage newPlay) {
        this.playID = newPlay.getID();
        this.winnerPlayer = "";
        this.loserPlayer = "";
        this.createdBy = newPlay.getP1();
        this.gameType = newPlay.getGameTypeEnum();
        this.playType = newPlay.getPlayTypeEnum();
        this.spectatorList = "";
    }

    public PlayEntity(RequestCreateTournamentMessage newPlay, String username) {
        this.playID = newPlay.getTournamentID();
        this.winnerPlayer = "";
        this.loserPlayer = "";
        this.createdBy = username;
        this.gameType = newPlay.getTournamentGameType();
        this.playType = PlayTypeEnum.TOURNAMENT;
        this.spectatorList = "";
    }

    public PlayEntity(CompletedTournamentMessage completedTournamentMessage) {
        this.playID = completedTournamentMessage.getId();
        this.winnerPlayer = completedTournamentMessage.getWinnerUsernames().toString();
    }

    public void addSpectator(String username) {
        if (!this.spectatorList.isEmpty()) {
            this.spectatorList += ",";
        }
        this.spectatorList += username;
    }

    public Set<String> getSpectators() {
        String[] users = this.spectatorList.split(",");
        return new HashSet<>(Arrays.asList(users));
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
                ", spectatorList=" + spectatorList +
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
        if (spectatorList != null ? !spectatorList.equals(that.spectatorList) : that.spectatorList != null)
            return false;
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
        result = 31 * result + (spectatorList != null ? spectatorList.hashCode() : 0);
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
