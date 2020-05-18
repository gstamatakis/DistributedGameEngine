package dto;

import game.GenericGameType;
import io.swagger.annotations.ApiModelProperty;
import model.PlayTypeEnum;

import java.time.LocalDateTime;

public class PlayStateResponseDTO {

    @ApiModelProperty()
    private String p1;
    @ApiModelProperty()
    private String ID;
    @ApiModelProperty()
    private String p2;
    @ApiModelProperty()
    private PlayTypeEnum playType;
    @ApiModelProperty()
    private GenericGameType gameType;
    @ApiModelProperty()
    private LocalDateTime createdAt;
    @ApiModelProperty()
    private int remainingRounds;

    public String getP1() {
        return p1;
    }

    public String getID() {
        return ID;
    }

    public String getP2() {
        return p2;
    }

    public PlayTypeEnum getPlayType() {
        return playType;
    }

    public GenericGameType getGameType() {
        return gameType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getRemainingRounds() {
        return remainingRounds;
    }
}
