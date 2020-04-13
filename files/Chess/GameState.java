import javax.swing.JOptionPane;



/**
 * This Class stores various information about the 'state'
 * of the game. Like whose turn it is to move and which unit
 * has been selected to be moved.
 * 
 * 
 */

public class GameState {

		
	static Allegiance turn = Allegiance.white;	// Turn to move ... black or white
	static boolean server = true;				// Is this the server or the client
	static ChessUnit current_unitSelected;	// The unit currently selected by the player for moving
	static King whiteTargetKing;							// The target (King unit) for the white player (used for check-mate)
	static King blackTargetKing;							// The target (King unit) for the black player (used for check-mate)
	static boolean ClientFound = false;						// Has a client been found (applicable for a Server only)
	
	/**
	 * Switch turns (who can move).
	 * To be invoked after making a move.
	 * 
	 */
	static void switchTurns() {

		if (turn == Allegiance.white) {
			turn = Allegiance.black;
			
			if (server) {
				
				UserInterface.textArea.setText("Please Wait for opponent (Black) to make Move.");
				
			}
			
			else {
				
				UserInterface.textArea.setText("Your Turn to make Move (Black).");
			}
			
			
		} else {
			turn = Allegiance.white;
			
			if (server) {

				UserInterface.textArea.setText("Your Turn to make Move (White).");

			}

			else {

				UserInterface.textArea.setText("Please Wait for opponent (White) to make Move.");
			}
			
		}
	}	
		
	private static void Restart() {
		
		
		turn = Allegiance.white;	
		server = true;				
		current_unitSelected = null;
		whiteTargetKing = null;			
		blackTargetKing = null;			
		ClientFound = false;	
		
		if (UserInterface.canvas != null)
			UserInterface.canvas.dispose();
		
		Thread newGame = new Thread(new Runnable() {

			@Override
			public void run() {
			
				UserInterface.main(null);

			}
		});

		newGame.start();
	
	}
	
	
	static void exit(boolean FirstToLeave) {
		
		
		if (FirstToLeave == true) {
		
			RMI.terminateConnection();
			
		}
		
	
		System.exit(0);
		
		
	}
	
	static void restartMessage() {
		
		Object[] options = {"Start New Game","Quit Game"};
		int n = JOptionPane.showOptionDialog(null,
				"Would you like to start a New Game or would you like to Leave?",
				"Choose", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

		if (n == -1)
			
			System.exit(0);
		
		if (n == 0) {
			
				Restart();		
		}

		if (n == 1) {
			
			System.exit(0);
		}

		
		
	}
	
	
}
