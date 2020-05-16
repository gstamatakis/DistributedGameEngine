package message.created;

import game.AbstractGameType;
import game.ChessGame;
import game.TicTacToeGame;
import message.completed.CompletedMoveMessage;
import model.PlayTypeEnum;

import java.time.LocalDateTime;

public class PlayStateMessage {
    private String p1, p2;
    private String ID;
    private PlayTypeEnum playType;
    private AbstractGameType gameType;
    private LocalDateTime createdAt;
    private int remainingRounds;

    public PlayStateMessage(PlayMessage play) {
        p1 = play.getP1();
        p2 = play.getP2();
        ID = play.getID();
        playType = play.getPlayType();
        createdAt = play.getCreatedAt();
        remainingRounds = play.getRemainingRounds();
        switch (play.getGameType()) {
            case TIC_TAC_TOE:
                gameType = new TicTacToeGame(p1);
                break;
            case CHESS:
                gameType = new ChessGame(p1);
                break;
            default:
                throw new IllegalStateException("Default case in PlayMessage 2nd constructor!");
        }
    }

    public CompletedMoveMessage considerMove(MoveMessage move) {
        return gameType.offerMove(move);
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

    public PlayTypeEnum getPlayType() {
        return playType;
    }

    public AbstractGameType getGameType() {
        return gameType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getRemainingRounds() {
        return remainingRounds;
    }
}
