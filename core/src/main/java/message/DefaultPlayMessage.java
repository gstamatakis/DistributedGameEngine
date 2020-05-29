package message;

import model.GameTypeEnum;

import java.io.Serializable;
import java.time.LocalDateTime;

public abstract class DefaultPlayMessage implements Serializable, PlayTypeMessage {
    private GameTypeEnum gameType;
    private String createdBy;
    private LocalDateTime createdAt;

    public DefaultPlayMessage(GameTypeEnum gameType, String createdBy) {
        this.gameType = gameType;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "DefaultPlayMessage{" +
                "gameType=" + gameType +
                ", createdBy='" + createdBy + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    public GameTypeEnum getGameType() {
        return gameType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
