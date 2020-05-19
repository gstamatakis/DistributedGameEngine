package game;

import message.completed.CompletedMoveMessage;
import message.created.MoveMessage;
import model.GameTypeEnum;

import java.util.HashMap;
import java.util.Map;

public class TicTacToeGameImpl extends AbstractGameType {
    private String p1, p2;

    public TicTacToeGameImpl(String playsFirst, String playsSecond) {
        super(playsFirst, playsSecond, GameTypeEnum.TIC_TAC_TOE);
        this.p1 = playsFirst;
        this.p2 = playsSecond;
    }

    @Override
    public CompletedMoveMessage offerMove(MoveMessage message) {
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
        return new CompletedMoveMessage(valid, playedBy, opponent, message, this.finished);
    }

    @Override
    public String emptyCell() {
        return "_";
    }

    @Override
    public Map<String, String> initialBoard() {
        HashMap<String, String> newBoard = new HashMap<>(9);
        for (int i = 0; i < 9; i++) {
            newBoard.put(String.valueOf(i), emptyCell());
        }
        return newBoard;
    }

    @Override
    public boolean isValidMove(MoveMessage message, Map<String, String> board) {
        String move = message.getMove();

        //Check if move is 1 digit
        if (move.length() != 1) {
            return false;
        }

        //Check if cell is already taken
        String cellContent = board.get(move);
        if (!cellContent.equals(emptyCell())) {
            return false;
        }

        try {
            int cellNo = Integer.parseInt(move);

            //Check if out of bounds
            if (cellNo < 1 || cellNo > 9) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TicTacToeGameImpl that = (TicTacToeGameImpl) o;

        if (p1 != null ? !p1.equals(that.p1) : that.p1 != null) return false;
        return p2 != null ? p2.equals(that.p2) : that.p2 == null;
    }

    @Override
    public int hashCode() {
        int result = p1 != null ? p1.hashCode() : 0;
        result = 31 * result + (p2 != null ? p2.hashCode() : 0);
        return result;
    }

    public String getP1() {
        return p1;
    }

    public String getP2() {
        return p2;
    }
}
