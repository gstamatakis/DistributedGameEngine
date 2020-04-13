public class Rook extends ChessUnit {

	Rook(Location location, Allegiance allegiance) {
		
		super(location, allegiance);
		
		this.movement_Type = Movement_Type.SearchTillBlocked;
		
		this.RawMoves.add(new Location(+1, 0));
		this.RawMoves.add(new Location(-1, 0));
		this.RawMoves.add(new Location(0, +1));
		this.RawMoves.add(new Location(0, -1));
		
		if (allegiance == Allegiance.white){
			
			this.graphicPath = "GameArt/White/Rook.png";
			
		}

		else if (allegiance == Allegiance.black){
			
			this.graphicPath = "GameArt/Black/Rook.png";
		
		}

		// Place Image on GUI
		GUI_ChessPanel.placeUnit(location, this);
		
		
	}
}
