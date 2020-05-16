package message.created;

import game.GameType;
import game.PlayType;

import java.time.LocalDateTime;

public class PlayStateMessage {
    private String p1, p2;
    private String ID;
    private PlayType playType;
    private GameType gameType;
    private LocalDateTime createdAt;
    private int remainingRounds;

    public PlayStateMessage(JoinedPlayMoveMessage value) {
        PlayMessage play = value.getPlay();
        p1 = play.getP1();
        p2 = play.getP2();
        ID = play.getID();
        playType = play.getPlayType();
        gameType = play.getGameType();
        createdAt = play.getCreatedAt();
        remainingRounds = play.getRemainingRounds();
        MoveMessage move = value.getMove();

    }

    public boolean considerMove(MoveMessage move) {
        return false;
    }

    public String getP1() {
        return p1;
    }

    public String getP2() {
        return p2;
    }

    public String getID() {
        return ID;
    }

    public PlayType getPlayType() {
        return playType;
    }

    public GameType getGameType() {
        return gameType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getRemainingRounds() {
        return remainingRounds;
    }
}
