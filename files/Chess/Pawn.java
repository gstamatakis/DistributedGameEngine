
public class Pawn extends ChessUnit {

	
	boolean first_Time_Moved = true;
	
	
	Pawn(Location location, Allegiance allegiance) {
		super(location, allegiance);
		
		this.movement_Type = Movement_Type.PawnMove;
		
		
		if (allegiance == Allegiance.white){
	
			this.graphicPath = "GameArt/White/Pawn.png";
			
			this.RawMoves.add(new Location(+1, +0));
			this.RawMoves.add(new Location(+2, +0));
			this.RawMoves.add(new Location(+1, +1));
			this.RawMoves.add(new Location(+1, -1));
			
			
		}

		else if (allegiance == Allegiance.black){
			
			this.graphicPath = "GameArt/Black/Pawn.png";
			
			this.RawMoves.add(new Location(-1, +0));
			this.RawMoves.add(new Location(-2, +0));
			this.RawMoves.add(new Location(-1, +1));
			this.RawMoves.add(new Location(-1, -1));
			
		}

		// Place Image on GUI
		GUI_ChessPanel.placeUnit(location, this);
		
	}
	
	/**
	 * Checks the pawn to see if it should be Promoted, and promots the pawn
	 * if needed.
	 *  
	 */
	
	void check_pawnPromotion() {

		Pawn unit = this;
		Location location = this.current_location;

		if (this.first_Time_Moved == true) {
			
			this.first_Time_Moved = false;
			
		}
		
		if ((location.Row == 8) && unit.isWhite()) {

			unit.killUnit();
			GUI_ChessPanel.removeUnit(location);
			new Queen(location, unit.allegiance);

		}

		if ((location.Row == 1) && unit.isBlack()) {

			unit.killUnit();
			GUI_ChessPanel.removeUnit(location);
			new Queen(location, unit.allegiance);

		}

	}

}
