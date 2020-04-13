import java.io.Serializable;

class Location implements Serializable{

	private static final long serialVersionUID = 1L;
	private static Location[][] locations;
	int Row;
	int Column;

	/** Initializations. Makes a Location Array
	 * 
	 */
	
	public static void initRows()
	{
		locations = new Location[9][9];
		for (int i = 0; i <= 8; i++) {

			for (int j = 0; j <= 8; j++) {
				
				locations[i][j] = new Location(i, j);

			}

		}

	}

	/** Returns true if two locations are one and the same.
	 * 
	 */
	
	boolean equals(Location location){
		
		if ((this.Row == location.Row) && (this.Column == location.Column))
			return true;
		else
			return false;
		
	}
	
	/** Returns a Location corresponding to the given Row and Column
	 * Throws an exception if location does not exist (see next function) 
	 */
	
	static Location getLocation(int row, int column) throws ArrayIndexOutOfBoundsException {
		
		
		if (new Location(row, column).location_exists())		
			return locations[row][column];
		else {
			
			throw new ArrayIndexOutOfBoundsException();
			
		}
	}

	
	Location(int row, int column) {
		Row = row;
		Column = column;
	}

	
	/** Returns false if a location does not exist on the chess board 
	 *  Only 1 <= Rows && Columns <= 8 exist ...  
	 * 
	 */
			
	boolean location_exists() {

		Location location = this;

		if ((location.Row > 8) || (location.Column > 8) || (location.Row < 1)
				|| (location.Column < 1)) {
			return false;
		}

		else {

			return true;
		}
	}

	/** Tells whether a Location is occupied or empty.
	 *  If occupied, also tells whether is occupied by a white
	 *  unit or a black unit.
	 */
	
	Square_Occupancy scoutLocation() {

		Location Target_location = this;
		ChessUnit TargetUnit = Board.getUnitAtLocation(Target_location);

		if (TargetUnit == null)
			return Square_Occupancy.Empty;
		if (TargetUnit.isWhite() == true)
			return Square_Occupancy.WhiteOccupied;
		if (TargetUnit.isBlack())
			return Square_Occupancy.BlackOccupied;

		System.out.println("Error in Class Location, method scout");
		return null;

	}
	
	/** Returns a location which is the sum of the current location and given Location
	 *  Used for getting 'absolute' locations from 'relative' locations for 
	 *  refining unit's moves 
	 * 
	 */
	
	Location SumLocation (Location locA){
		
		return getLocation(locA.Row + this.Row, locA.Column + this.Column);
				
	}
	

	
	
	
	
}