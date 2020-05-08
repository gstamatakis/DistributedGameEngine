package message;

import game.GameType;
import game.PlayType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PlayMessage {
    private GameType gameType;
    private PlayType playType;
    private String playID;
    private List<String> playerUsernames;
    private String tournamentID;
    private LocalDateTime createdAt;

    public PlayMessage() {
    }

    public PlayMessage(JoinPlayMessage msg1, JoinPlayMessage msg2) {
        this.gameType = msg1.getGameType();
        this.playType = msg1.getPlayType();
        this.tournamentID = msg1.getTournamentId();
        this.playerUsernames = new ArrayList<>();
        this.playerUsernames.add(msg1.getUsername());
        this.playerUsernames.add(msg2.getUsername());
        this.createdAt = java.time.LocalDateTime.now();
        this.playID = String.valueOf(hashCode());
    }

    @Override
    public String toString() {
        return "PlayMessage{" +
                "gameType=" + gameType +
                ", playType=" + playType +
                ", playID='" + playID + '\'' +
                ", playerUsernames=" + playerUsernames +
                ", tournamentID='" + tournamentID + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public int hashCode() {
        int result = gameType.hashCode();
        result = 31 * result + playType.hashCode();
        result = 31 * result + playerUsernames.hashCode();
        result = 31 * result + tournamentID.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayMessage that = (PlayMessage) o;

        if (gameType != that.gameType) return false;
        if (playType != that.playType) return false;
        if (!playerUsernames.equals(that.playerUsernames)) return false;
        return tournamentID.equals(that.tournamentID);
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public PlayType getPlayType() {
        return playType;
    }

    public void setPlayType(PlayType playType) {
        this.playType = playType;
    }

    public String getPlayID() {
        return playID;
    }

    public void setPlayID(String playID) {
        this.playID = playID;
    }

    public List<String> getPlayerUsernames() {
        return playerUsernames;
    }

    public void setPlayerUsernames(List<String> playerUsernames) {
        this.playerUsernames = playerUsernames;
    }

    public String getTournamentID() {
        return tournamentID;
    }

    public void setTournamentID(String tournamentID) {
        this.tournamentID = tournamentID;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
