/**
 * The WorkerThread (actually not a thread at all) has a method called doWork() which is called whenever the user clicks the board.
 * This class\method then creates a "real" seperate thread for ther operation and executes it
 *
 * @author 13020348
 */

public class WorkerThread {

    private static Location location;
    private static boolean movestate = false;
    //private static ChessUnit unitSelected = null;


    /**
     * The WorkerThread (actually not a thread at all) has a method called doWork() which is called whenever the user clicks the board.
     * This class\method then creates a "real" seperate thread for ther operation and executes it
     */


    static void doWork(Location location_in) {

        location = location_in;


        if (((GameState.server == true) && (GameState.turn == Allegiance.black)) ||    // This is not your turn to move.
                ((GameState.server == false) && (GameState.turn == Allegiance.white)))        // So display a message saying that and return.

        {

            JOptionPane.showMessageDialog(null, "This is not your turn to move.", "Not your Turn", JOptionPane.ERROR_MESSAGE);
            return;
        }


        // First Click (to Select a Unit)

        if (movestate == false) {

            GameState.current_unitSelected = Board.getUnitAtLocation(location);

            if (GameState.current_unitSelected == null) // Clicked on an empty square ... so
                return;                // nothing to do.


            // Not needed?
            if ((GameState.turn == Allegiance.white) && (GameState.current_unitSelected.isBlack()) ||
                    (GameState.turn == Allegiance.black) && (GameState.current_unitSelected.isWhite())) {

                JOptionPane.showMessageDialog(null, "This is not your turn to move.", "Not your Turn", JOptionPane.ERROR_MESSAGE);
                return;

            }


            Thread newSelectThread = new selectUnit(location);    // Start a new Thread to do the "Unit Selection" processes ... like Hilighting squares
            newSelectThread.start();
            movestate = true;
        }

        // Second Click ... to tell the previously selected unit to move to the now-clicked location

        else {

            // Reset any Hilighting (since move will now be made)


            GUI_ChessPanel.chessSquare_Array[GameState.current_unitSelected.current_location.Row][GameState.current_unitSelected.current_location.Column].ResetSquareColor();
            for (int i = 0; i < GameState.current_unitSelected.RefinedMoves.size(); i++) {

                Location tempLoc = GameState.current_unitSelected.RefinedMoves.get(i);
                GUI_ChessPanel.chessSquare_Array[tempLoc.Row][tempLoc.Column].ResetSquareColor();
            }

            // Reset click State ... next click will be to select unit not to move anything.

            movestate = false;


            // If the clicked Location is not a legal Position to move the unit to according to the rules
            // Then show a message and do nothing more.


            if (ChessUnit.Check() == true) {
                if ((GameState.current_unitSelected.validateMove(location) == true)
                        && (ChessUnit.validateforCheck(
                        GameState.current_unitSelected, location) == false)) {

                    JOptionPane
                            .showMessageDialog(
                                    null,
                                    "You can not make this move. You must remove the check on your King (if possible)",
                                    "Remove Check First",
                                    JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (GameState.current_unitSelected.validateMove(location) == false) {

                // If selected own unit then select that instead.
                if ((GameState.current_unitSelected.isWhite() && location
                        .scoutLocation() == Square_Occupancy.WhiteOccupied)
                        || (GameState.current_unitSelected.isBlack() && location
                        .scoutLocation() == Square_Occupancy.BlackOccupied)) {

                    doWork(location);
                    return;

                }

                JOptionPane.showMessageDialog(null, "This unit can not move to the given position", "Illegal Move", JOptionPane.ERROR_MESSAGE);
                return;

            }


            // otherwise ... is a legal move ... so start a new thread to move the unit
            // the actual move is a seperate method with a seperate thread because
            // it can also be called by the other player over the network.

            Thread newMoveThread = new moveUnit(GameState.current_unitSelected, location);
            newMoveThread.start();
            movestate = false;

            // BroadCast the move to the other player through RMI
            RMI.broadcastMove(GameState.current_unitSelected.current_location, location);


        }

    }

}

// Select Unit Thread.
class selectUnit extends Thread {

    Location Location;

    selectUnit(Location location) {
        super();
        Location = location;
    }

    @Override
    public void run() {
        super.run();

        ChessUnit unit = Board.getUnitAtLocation(Location);
        ChessSquare.ColoriseMoves(unit);    // Hilight the squares the selected unit can move to.
    }

}

class moveUnit extends Thread {

    Location location;
    ChessUnit unit;

    moveUnit(ChessUnit unit_in, Location location_in) {
        super();
        location = location_in;
        unit = unit_in;
    }


    public void run() {
        super.run();


        // Move the unit in both the chess rule engine and the GUI (graphics)

        GUI_ChessPanel.placeUnit(location, unit);
        GUI_ChessPanel.removeUnit(unit.current_location);
        unit.makeMove(location);


        // Check for Pawn Promotion
        if (unit.getClass() == Pawn.class) {

            Pawn pawn = (Pawn) unit;
            pawn.check_pawnPromotion();

        }

        GameState.switchTurns();

        // If a unit has the opposing King in check then display a winner message and close the game.


        if ((ChessUnit.Check() == true)) {

            String winner = GameState.turn.toString() + " is in Check.";
            JOptionPane.showMessageDialog(null, winner, "Check", JOptionPane.INFORMATION_MESSAGE);

        }
		/*
		if (ChessUnit.CheckMate()) {

			String winner = "CheckMATE ... " + GameState.turn.toString() + " wins the game!";
			JOptionPane.showMessageDialog(null,  winner ,"Check",JOptionPane.INFORMATION_MESSAGE);
			GameState.restartMessage();
		
		}
		
		*/

    }

}
