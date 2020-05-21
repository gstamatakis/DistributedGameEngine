package game;

import message.completed.CompletedMoveMessage;
import message.created.MoveMessage;
import model.GameTypeEnum;

import java.util.HashMap;
import java.util.Map;

public class ChessGameState extends AbstractGameState {
    public ChessGameState() {
    }

    public ChessGameState(String playsFirst, String playsSecond, String createdBy) {
        super(playsFirst, playsSecond, GameTypeEnum.CHESS, createdBy);
    }

    @Override
    public String toString() {
        return "ChessGameState{" +
                "board=" + board +
                ", playsFirstUsername='" + playsFirstUsername + '\'' +
                ", playsSecondUsername='" + playsSecondUsername + '\'' +
                ", movesPerRoundP1=" + movesPerRoundP1 +
                ", movesPerRoundP2=" + movesPerRoundP2 +
                ", currentRound=" + currentRound +
                ", lastValidMove=" + lastValidMove +
                ", gameTypeEnum=" + gameTypeEnum +
                ", winner='" + winner + '\'' +
                ", createdBy='" + createdBy + '\'' +
                '}';
    }

    @Override
    public CompletedMoveMessage offerMove(MoveMessage message) {
        boolean finished = false;//TODO change
        String playedBy = message.getUsername();
        String opponent = getPlaysFirstUsername().equals(playedBy)
                ? getPlaysSecondUsername()
                : getPlaysFirstUsername();
        boolean valid = isValidMove(message, getBoard());
        if (valid) {
            Map<Integer, MoveMessage> movesPerRound = getPlaysFirstUsername().equals(playedBy)
                    ? getMovesPerRoundP1()
                    : getMovesPerRoundP2();
            movesPerRound.put(getCurrentRound(), message);
            if (playedBy.equals(getPlaysFirstUsername())) {
                this.currentRound++;
            }
        }
        return new CompletedMoveMessage(valid, playedBy, opponent, message, finished);
    }

    @Override
    public Map<String, String> initialBoard() {
        Map<String, String> newBoard = new HashMap<>();

        //Add white pawns
        char col = 'A';
        for (int i = 1; i <= 8; i++) {
            newBoard.put("2" + col++, "WP");
        }
        //Add the rest of the white pieces
        newBoard.put("1A", "WR");
        newBoard.put("1B", "WK");
        newBoard.put("1C", "WB");
        newBoard.put("1D", "WQ");
        newBoard.put("1E", "WK");
        newBoard.put("1F", "WB");
        newBoard.put("1G", "WK");
        newBoard.put("1H", "WR");

        //Add black pawns
        col = 'A';
        for (int i = 1; i <= 8; i++) {
            newBoard.put("7" + col++, "BP");
        }
        //Add the rest of the black pieces
        newBoard.put("8A", "BR");
        newBoard.put("8B", "BK");
        newBoard.put("8C", "BB");
        newBoard.put("8D", "BQ");
        newBoard.put("8E", "BK");
        newBoard.put("8F", "BB");
        newBoard.put("8G", "BK");
        newBoard.put("8H", "BR");

        return newBoard;
    }

    @Override
    public boolean isValidMove(MoveMessage message, Map<String, String> board) {
        return false;
    }

    @Override
    public String emptyCell() {
        return "_";
    }
}
