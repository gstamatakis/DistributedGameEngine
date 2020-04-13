
public class Board {

	static ChessUnit[][] board;
	
	/**
	 * Create units
	 */
	
	@SuppressWarnings("unused")
	static void createBoard(){
		

		board = new ChessUnit[9][9];
		ChessUnit.BlackUnitRoll.clear();
		ChessUnit.WhiteUnitRoll.clear();
		
		King kingWhite = new King(Location.getLocation(1, 5),Allegiance.white);
		King kingBlack = new King(Location.getLocation(8, 5),Allegiance.black);
		
		Queen queenWhite = new Queen(Location.getLocation(1, 4),Allegiance.white);
		Queen queenBlack = new Queen(Location.getLocation(8, 4),Allegiance.black);
		
		Bishop bishopWhite01 = new Bishop(Location.getLocation(1, 3),Allegiance.white);
		Bishop bishopWhite02 = new Bishop(Location.getLocation(1, 6),Allegiance.white);
		
		Bishop bishopBlack01 = new Bishop(Location.getLocation(8, 3),Allegiance.black);
		Bishop bishopBlack02 = new Bishop(Location.getLocation(8, 6),Allegiance.black);
				
		Knight knightWhite01 = new Knight(Location.getLocation(1, 2),Allegiance.white);
		Knight knightWhite02 = new Knight(Location.getLocation(1, 7),Allegiance.white);
		
		Knight knightBlack01 = new Knight(Location.getLocation(8, 2),Allegiance.black);
		Knight knightBlack02 = new Knight(Location.getLocation(8, 7),Allegiance.black);
		
		Rook rookWhite01 = new Rook(Location.getLocation(1, 1),Allegiance.white);
		Rook rookWhite02 = new Rook(Location.getLocation(1, 8),Allegiance.white);
		
		Rook rookBlack01 = new Rook(Location.getLocation(8, 1),Allegiance.black);
		Rook rookBlack02 = new Rook(Location.getLocation(8, 8),Allegiance.black);
		
		for (int i = 1; i < 9; i++){
			
			
			new Pawn(Location.getLocation(2, i),Allegiance.white);
			new Pawn(Location.getLocation(7, i),Allegiance.black);
			
		}
		
	}
	
	
	static ChessUnit getUnitAtLocation(Location location) {

		try {
			return board[location.Row][location.Column];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}

	}
	
	static void moveUnit(ChessUnit unit_in, Location location){
		
		if (getUnitAtLocation(location) != null) {
			
			getUnitAtLocation(location).killUnit();
			
		}
		
		Location oldLocation = unit_in.current_location;
		board[oldLocation .Row][oldLocation .Column] = null;
		board[location.Row][location.Column] = unit_in;
		
		
				
	}
	
	static void removeUnit(Location location) {

		if (board[location.Row][location.Column] == null) {

			System.out.println("Location is already Empty");
			return;

		}

		else {

			board[location.Row][location.Column] = null;

		}
		
	}
	
	static void placeUnit(ChessUnit unit_in, Location location){
		
		
		if (board[location .Row][location .Column] != null){
			
			System.out.println("Location is already Occupied");
			return;
			
		}
		
		else{
			
			board[location.Row][location.Column] = unit_in;
			
			
		}
		
		
		
		
	}
	
	
	
}
