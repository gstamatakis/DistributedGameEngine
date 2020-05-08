package message;

import game.GameType;
import game.PlayType;

import java.io.Serializable;
import java.time.LocalDateTime;

public class JoinPlayMessage implements Serializable {
    private PlayType playType;
    private GameType gameType;
    private String tournamentId;
    private LocalDateTime timeSent;
    private String username;

    public JoinPlayMessage() {
    }

    public JoinPlayMessage(String username, PlayType playType, GameType gameType) {
        this.timeSent = java.time.LocalDateTime.now();
        this.username = username;
        this.playType = playType;
        this.gameType = gameType;
        this.tournamentId = "";
    }

    public JoinPlayMessage(String username, PlayType playType, GameType gameType, String tournamentId) {
        this.timeSent = java.time.LocalDateTime.now();
        this.username = username;
        this.playType = playType;
        this.gameType = gameType;
        this.tournamentId = tournamentId;
    }

    public boolean isPracticePlay(){
        return this.tournamentId.isEmpty();
    }

    public boolean isTournamentPlay(){
        return !this.tournamentId.isEmpty();
    }

    /**
     * ID of this message.
     * Unique per user, gameType, playType.
     *
     * @return A String hash of the fields that should be unique in a state store.
     */
    public String key() {
        return String.valueOf(String.format("%s,%s,%s", playType.toString(), gameType.toString(), username).hashCode());
    }

    @Override
    public String toString() {
        return "JoinPlayMessage{" +
                "playType=" + playType +
                ", gameType=" + gameType +
                ", tournamentId='" + tournamentId + '\'' +
                ", timeSent=" + timeSent +
                ", username='" + username + '\'' +
                '}';
    }

    public LocalDateTime getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(LocalDateTime timeSent) {
        this.timeSent = timeSent;
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public PlayType getPlayType() {
        return playType;
    }

    public void setPlayType(PlayType playType) {
        this.playType = playType;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }
}

