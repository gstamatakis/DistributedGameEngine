package message;

import game.GameType;

import java.io.Serializable;
import java.time.LocalDateTime;

public abstract class DefaultPlayMessage implements Serializable, PlayTypeMessage {
    private GameType gameType;
    private String createdBy;
    private LocalDateTime createdAt;

    public DefaultPlayMessage(GameType gameType, String createdBy) {
        this.gameType = gameType;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public GameType getGameType() {
        return gameType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
