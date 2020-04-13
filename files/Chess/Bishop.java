public class Bishop extends ChessUnit {

	Bishop(Location location, Allegiance allegiance) {

		super(location, allegiance);
				
		
		this.movement_Type = Movement_Type.SearchTillBlocked;

		this.RawMoves.add(new Location(+1, +1));
		this.RawMoves.add(new Location(-1, -1));
		this.RawMoves.add(new Location(-1, +1));
		this.RawMoves.add(new Location(+1, -1));
		
		if (this.allegiance == Allegiance.white){
			
			this.graphicPath = "GameArt/White/Bishop.png";
			
		}

		else if (this.allegiance == Allegiance.black){
			
			this.graphicPath = "GameArt/Black/Bishop.png";
		
		}

		
		GUI_ChessPanel.placeUnit(location, this);

	}
}
