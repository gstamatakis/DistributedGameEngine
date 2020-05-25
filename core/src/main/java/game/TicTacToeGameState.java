package game;

import message.completed.CompletedMoveMessage;
import message.created.MoveMessage;
import model.GameTypeEnum;

import java.util.HashMap;
import java.util.Map;

public class TicTacToeGameState extends AbstractGameState {
    private String p1, p2;

    public TicTacToeGameState() {
    }

    public TicTacToeGameState(String playsFirst, String playsSecond, String createdBy) {
        super(playsFirst, playsSecond, GameTypeEnum.TIC_TAC_TOE, createdBy);
        this.p1 = playsFirst;
        this.p2 = playsSecond;
    }

    @Override
    public String toString() {
        return "TicTacToeGameState{" +
                "p1='" + p1 + '\'' +
                ", p2='" + p2 + '\'' +
                ", board=" + board +
                ", playsFirstUsername='" + playsFirstUsername + '\'' +
                ", playsSecondUsername='" + playsSecondUsername + '\'' +
                ", movesPerRoundP1=" + movesPerRoundP1 +
                ", movesPerRoundP2=" + movesPerRoundP2 +
                ", currentRound=" + currentRound +
                ", lastValidMove=" + lastValidMoveMessage +
                ", gameTypeEnum=" + gameTypeEnum +
                ", winner='" + winner + '\'' +
                ", createdBy='" + createdBy + '\'' +
                '}';
    }

    @Override
    public CompletedMoveMessage offerMove(MoveMessage message) {
        String move = message.getMove();
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
            getBoard().put(move, playedBy);
            checkForVictor();
            this.lastValidMoveMessage = message;
            if (playedBy.equals(getPlaysFirstUsername())) {
                this.currentRound++;
            }
        }
        return new CompletedMoveMessage(valid, playedBy, opponent, message, finished);
    }

    private void checkForVictor() {

        //Check horizontally
        if (!board.get("1").equals(emptyCell()) && board.get("1").equals(board.get("2")) && board.get("1").equals(board.get("3"))) {
            this.winner = board.get("1").equals(p1) ? -1 : 1;
            this.finished = true;
        }
        if (!board.get("4").equals(emptyCell()) && board.get("4").equals(board.get("5")) && board.get("4").equals(board.get("6"))) {
            this.winner = board.get("4").equals(p1) ? -1 : 1;
            this.finished = true;
        }
        if (!board.get("7").equals(emptyCell()) && board.get("7").equals(board.get("8")) && board.get("7").equals(board.get("9"))) {
            this.winner = board.get("7").equals(p1) ? -1 : 1;
            this.finished = true;
        }

        //Check vertically
        if (!board.get("1").equals(emptyCell()) && board.get("1").equals(board.get("4")) && board.get("1").equals(board.get("7"))) {
            this.winner = board.get("1").equals(p1) ? -1 : 1;
            this.finished = true;
        }
        if (!board.get("2").equals(emptyCell()) && board.get("2").equals(board.get("5")) && board.get("2").equals(board.get("8"))) {
            this.winner = board.get("2").equals(p1) ? -1 : 1;
            this.finished = true;
        }
        if (!board.get("3").equals(emptyCell()) && board.get("3").equals(board.get("6")) && board.get("3").equals(board.get("9"))) {
            this.winner = board.get("3").equals(p1) ? -1 : 1;
            this.finished = true;
        }

        //Check diagonally
        if (!board.get("1").equals(emptyCell()) && board.get("1").equals(board.get("5")) && board.get("1").equals(board.get("9"))) {
            this.winner = board.get("1").equals(p1) ? -1 : 1;
            this.finished = true;
        }
        if (!board.get("3").equals(emptyCell()) && board.get("3").equals(board.get("5")) && board.get("3").equals(board.get("7"))) {
            this.winner = board.get("3").equals(p1) ? -1 : 1;
            this.finished = true;
        }
    }

    private boolean cellIsMine(int cell, String username) {
        return this.board.get(String.valueOf(cell)).equals(username);
    }

    @Override
    public String emptyCell() {
        return "_";
    }

    @Override
    public Map<String, String> initialBoard() {
        HashMap<String, String> newBoard = new HashMap<>(9);
        for (int i = 1; i <= 9; i++) {
            newBoard.put(String.valueOf(i), emptyCell());
        }
        return newBoard;
    }

    @Override
    public boolean isValidMove(MoveMessage message, Map<String, String> board) {
        String move = message.getMove();

        if (move == null) {
            return false;
        }

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

        TicTacToeGameState that = (TicTacToeGameState) o;

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
