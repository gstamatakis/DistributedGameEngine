import java.util.ArrayList;

import javax.swing.JOptionPane;

/**
 * The ChessUnit class is the super-class of all units ... like King, Queen,
 * Pawn, Knight etc. and stores fields and methods common to all units.
 * 
 * 
 * @author 13020348
 * 
 */

class ChessUnit extends Board implements ChessPiece {

	protected String graphicPath;	// Stores the path of the image file used by the unit.
	protected	boolean alive;		// False for captured units
	protected Allegiance allegiance;	// The color of the unit ... black or white
	protected Location current_location;	// the current location of the unit
	
	/**
	 * List of all White Units
	 */
	
	protected static ArrayList<ChessUnit> WhiteUnitRoll = new ArrayList<ChessUnit>();
	
	/**
	 * List of all Black Units
	 */
	
	
	protected static ArrayList<ChessUnit> BlackUnitRoll = new ArrayList<ChessUnit>();

	
	/**
	 * Movement Type (three types exist ... SingleHop ... like a Knight or King
	 * SearchTillBlocked ... a sweep until cut-off move made by bishops, queens and rooks
	 * and PawnMove ... the unique moves of a Pawn.
	 * 
	 * Used in conjustion with RawMoves (see below)
	 * 
	 */
		
	protected Movement_Type movement_Type;	
	
	/**
	 * List representing the "Raw Moves" that can be made by the unit in "theory". For exmple a rook can move any number of 
	 * un-occupied squares horizintally or vertically. A King an move one square in any direction etc.
	 */
	
	protected ArrayList<Location> RawMoves = new ArrayList<Location>();
	
	/**
	 * List of "Refined Moves" that can be made by the unit. This is made by removing illegal or impossible
	 * moves from the RawMoves list. For exmple a rook can not move beyond 
	 * the boundaries of the board or on an already occupied square ... so these locations will be removed
	 * from the raw moves to get the refined moves.
	 * 
	 */
	
	
	ArrayList<Location> RefinedMoves = new ArrayList<Location>();

	/**
	 * Unit which killed me ;)
	 */
	
	ChessUnit murderer = null;
	
	
	/** ChessUnit Class Constructor
	 * 
	 * @param location
	 * @param allegiance_in
	 */
	
	ChessUnit(Location location, Allegiance allegiance_in){
		
		current_location = location;	// Store Location
		Board.placeUnit(this, location);	// Store Unit in Board Array
		allegiance = allegiance_in;			// Assign Color of unit (Black\White)
		alive = true;						// Newly-Created units are alive ;)
		

		if (this.isBlack())					// Add unit to the collection of its corresponding color
			BlackUnitRoll.add(this);
		else
			WhiteUnitRoll.add(this);
		
		
	}
	/**
	 * Returns true if a checkmate condition exists.
	 * That is the current player can not make any move which can save his\her
	 * king from getting captured.
	 * 
	 * @return
	 */
	
	static boolean CheckMate(){
		
		ArrayList<ChessUnit> roll = null;
		@SuppressWarnings("unused")
		King targetKing = null;
		
		
		if (GameState.turn == Allegiance.black)
		{
			roll = BlackUnitRoll;
			targetKing = GameState.whiteTargetKing;
			
		}
		
		if (GameState.turn == Allegiance.white)
		{
			roll = WhiteUnitRoll;
			targetKing = GameState.blackTargetKing;
		}
		
		
		for (int i = 0; i < roll.size(); i++) {

			roll.get(i).refineMoves();

			for (int j = 0; j < roll.get(i).RefinedMoves.size(); j++) {

				if (roll.get(i).alive == true) {

					if (validateforCheck(roll.get(i),
							roll.get(i).RefinedMoves.get(j))) {

						return false;

					}

				}
			}
		}

	return true;
		
	}
	
	/** Returns if the current unit can move to the given Location according
	 * to the rules of chess.
	 * 
	 * Works by looking up the list of refined moves of the Unit and
	 * searching for the provided Location within that list.
	 * 
	 */
	
	public boolean validateMove(Location location){
		
				
		boolean flag = false;
				
		for (int i = 0; i < this.RefinedMoves.size(); i++){
						
			if (location.equals(this.RefinedMoves.get(i))){
				flag = true;
				break;
			}
				
			
		}
			
		return flag;
	}

	/** Returns the locations the king can move to to save
	 *  himself from a check
	 * 
	 * @return
	 */
	
	public static ArrayList<Location> kingRun_from_threat() {
		
		
		ArrayList<Location> kingsMoves = new ArrayList<Location>();
		ArrayList<ChessUnit> roll = null;
		King kingtoSave = null;
		
		
		if (GameState.turn == Allegiance.black)
		{
			roll = WhiteUnitRoll;
			kingtoSave = GameState.whiteTargetKing;
			
		}
		
		if (GameState.turn == Allegiance.white)
		{
			roll = BlackUnitRoll;
			kingtoSave = GameState.blackTargetKing;
		}
		
		kingtoSave.refineMoves();
		kingsMoves = kingtoSave.RefinedMoves;
		
		for (int i = 0; i < roll.size(); i++) {

			for (int j = 0; j < roll.get(i).RefinedMoves.size(); j++) {

				for (int k = 0; k < kingsMoves.size(); k++) {

					if (roll.get(i).RefinedMoves.get(j).equals(kingsMoves.get(k))) {

						kingsMoves.remove(k);

					}

				}

			}
			
			
		}
				

		return kingsMoves;
		
		
		
	}
	
	
	/** Moves the unit to the given Location.
	 * 
	 * Also updates its position within the board Array.
	 * 
	 */
	
	public boolean makeMove(Location location) {

		try {
			Board.moveUnit(this, location);
			current_location = location;
			return true;
		} catch (Exception e) {

			return false;

		}

	}
	
	/**
	 * Kills the given unit.
	 * Also removes it from the board array.
	 *  Killed units are still present within the unit roll-register
	 * 
	 */
	
	void killUnit() {
		this.alive = false;
		Board.removeUnit(this.current_location);
		
		
	}
	
	/** Returns true if the unit belongs to the White faction
	 * 
	 */
	
	public boolean isWhite() {

		if (allegiance == Allegiance.white)
			return true;
		else
			return false;

	}

	/** Returns true if the unit belongs to the Black faction
	 * 
	 */
	
	
	public boolean isBlack() {

		if (allegiance == Allegiance.white)
			return false;
		else
			return true;

	}

	
	/**
	 * Refines the unit's "Raw-Move" list.
	 * Call this to make/update the "Refined Moves" list.
	 */
	
	void refineMoves() {

		// Clear the old RefinedMoves List
		RefinedMoves.clear();	
		
		// If is SIngleHop tyoe ... like King or Knight


		
		if (this.movement_Type == Movement_Type.SingleHop) 
		{
			
		for (int i = 0; i < this.RawMoves.size(); i++) {		// Iterate through the RawMoves List

			Location targetLocation = this.current_location;
		
			
			try{					
				targetLocation = targetLocation.SumLocation(RawMoves.get(i));	// Use the 'relative' new location to get an 'absolute' new location
			
				if (targetLocation.location_exists())	// Make sure Location exists on board ... i.e its not row -4 or column +13 etc
					
					if		(targetLocation.scoutLocation() == Square_Occupancy.Empty || 			// If Location is empty
							(targetLocation.scoutLocation() == Square_Occupancy.BlackOccupied && this.isWhite()) ||		// Or occupied by an enemy unit
							(targetLocation.scoutLocation() == Square_Occupancy.WhiteOccupied && this.isBlack()))
								this.RefinedMoves.add(targetLocation);							// Add it to the refined moves list.
				} catch (ArrayIndexOutOfBoundsException e) {
					
				}
	
		
		}
		
		}

		// If is a Sweep Across type move ... like the Queen, Bishop and Rook
		
		else if (this.movement_Type == Movement_Type.SearchTillBlocked){
			
			
			
			for (int i = 0; i < this.RawMoves.size(); i++) {		// Iterate through the RawMoves List. 
				Location targetLocation = this.current_location;
				
				while (true) {										// In simple words ... do a recursive ... incremental search search for locations till-blocked ...  continue summing up the 
																	// 'relative' new location.

					try{					
					targetLocation = targetLocation.SumLocation(RawMoves.get(i));	// Use the 'relative' new location to get an 'absolute' new location
					}
					catch(ArrayIndexOutOfBoundsException e){
						break;
					}

					if (targetLocation.location_exists() == false)													// Make sure Location exists on board ... i.e its not row -4 or column +13 etc	
						break;
					if	((targetLocation.scoutLocation() == Square_Occupancy.BlackOccupied && this.isBlack()) ||	// If new location is Occupied by Ally Unit ... don't add that location to Refined List
						(targetLocation.scoutLocation() == Square_Occupancy.WhiteOccupied && this.isWhite()))		// And stop the incremental addition from there
						break;
					if	((targetLocation.scoutLocation() == Square_Occupancy.BlackOccupied && this.isWhite()) ||	// If new location is occupied by enemy unit ... add the last square (enemy occupied) to the list and stop
						(targetLocation.scoutLocation() == Square_Occupancy.WhiteOccupied && this.isBlack())){		// the incremental search from there
						this.RefinedMoves.add(targetLocation);
						break;
					}
								
					this.RefinedMoves.add(targetLocation);														// Add the Location to the list
					
				}
				
			}
			
			
			
		}

		// If this is a pawn
		
		else if (this.movement_Type == Movement_Type.PawnMove) {

			Pawn pawn = (Pawn) this;	// Cast the unit to Pawn ... so that we can use some Pawn-Specific fields (firstTImeMoved)

			try {
				Location targetLocation = this.current_location		// The first move (one squar forward)
						.SumLocation(RawMoves.get(0));

				if (targetLocation.scoutLocation() == Square_Occupancy.Empty) {

					this.RefinedMoves.add(targetLocation);		

					if (pawn.first_Time_Moved == true) {		// If this is the Pawn's first move then it can also move two squares forward

						targetLocation = this.current_location
								.SumLocation(RawMoves.get(1));
						if (targetLocation.scoutLocation() == Square_Occupancy.Empty) {

							this.RefinedMoves.add(targetLocation);

						}

					}

				}

			} catch (ArrayIndexOutOfBoundsException e) {

			}

			try {
				Location targetLocation = this.current_location
						.SumLocation(RawMoves.get(2));

				if (((targetLocation.scoutLocation() == Square_Occupancy.BlackOccupied) && this	// If the two foward diagonal squares are occupied by an enemy unit ... the pawn can capture them
						.isWhite())
						|| ((targetLocation.scoutLocation() == Square_Occupancy.WhiteOccupied) && this
								.isBlack())) {

					this.RefinedMoves.add(targetLocation);

				} 

			} catch (ArrayIndexOutOfBoundsException e) {

			}

			try {
				Location targetLocation = this.current_location
						.SumLocation(RawMoves.get(3));

				if (((targetLocation.scoutLocation() == Square_Occupancy.BlackOccupied) && this
						.isWhite())
						|| ((targetLocation.scoutLocation() == Square_Occupancy.WhiteOccupied) && this
								.isBlack())) {

					this.RefinedMoves.add(targetLocation);

				}

			} catch (ArrayIndexOutOfBoundsException e) {
					// Non-Existant locations throw this exception ... i.e its not row -4 or column +13 etc
			}

		}

	}

		
	/** Returns true if the current unit has a check on the opposing king.
	 * 
	 */
	
	public boolean hasCheckOnOpposingKing(Location positionOfOpposingKing) {

		this.refineMoves();

		for (int i = 0; i < this.RefinedMoves.size(); i++) {

			if (this.RefinedMoves.get(i).equals(positionOfOpposingKing)) {
				return true;
			}

		}

		return false;
	}
	
	/** Returns true if the unit is alive (not captured yet).
	 * 
	 */
	
	public boolean isActive() {

		return alive;
	}

	
	/** Returns a list of units which are threatnig the King
	 * 
	 * 
	 * @return
	 */
	
	    static ArrayList<ChessUnit> getUnits_that_threaten() {

		ArrayList<ChessUnit> checkingUnits = new ArrayList<ChessUnit>();
		ArrayList<ChessUnit> roll = null;
		King targetKing = null;

		if (GameState.turn == Allegiance.black) {
			roll = WhiteUnitRoll;
			targetKing = GameState.whiteTargetKing;

		}

		if (GameState.turn == Allegiance.white) {
			roll = BlackUnitRoll;
			targetKing = GameState.blackTargetKing;
		}

		for (int i = 0; i < roll.size(); i++) {

			roll.get(i).refineMoves();

			for (int j = 0; j < roll.get(i).RefinedMoves.size(); j++) {

				if (roll.get(i).alive == true) {

					if (Board
							.getUnitAtLocation(roll.get(i).RefinedMoves.get(j)) == targetKing) {
						
						checkingUnits.add(roll.get(i));
						break;		

					}

				}
			}
		}

		return checkingUnits;

	}
	    
	/** Returnn a list of units that can kill the unit
	 * 
	 * @return
	 */
	
	ArrayList<ChessUnit> getUnits_ThatCan_removeThreat() {
		
		ChessUnit unit = this;
		ArrayList<ChessUnit> threatRemovingUnits = new ArrayList<ChessUnit>();
		ArrayList<ChessUnit> roll = null;
			
		
		if (GameState.turn == Allegiance.white)
		{
			roll = WhiteUnitRoll;
			
			
		}
		
		if (GameState.turn == Allegiance.black)
		{
			roll = BlackUnitRoll;
			
		}
		
		
		for (int i = 0; i < roll.size(); i++) {

			roll.get(i).refineMoves();

			for (int j = 0; j < roll.get(i).RefinedMoves.size(); j++) {

				if (roll.get(i).alive == true) {

					if (Board.getUnitAtLocation(roll.get(i).RefinedMoves.get(j)) == unit) {
						threatRemovingUnits.add(roll.get(i));
						break;

					}

				}
			}
		}
			
		return threatRemovingUnits;	
			
			
		}
	
	/** Validate Move when Player is in check
	 *  Returns false unless the move (location and unit pair)
	 *  will result in breaking the check
	 * 
	 * @param unit
	 * @param loc
	 * @return
	 */
		
	
		static boolean validateforCheck(ChessUnit unit, Location loc) {
			
			
			// See if we can move the king away to avoid capture 
			
			ArrayList<Location> kingMoves = kingRun_from_threat();
			
			for (int i = 0; i < kingMoves.size(); i++) {
			
				if ((unit.getClass() == King.class) && (loc.equals(kingMoves.get(i)))) {
					
					Thread thread = new Thread(new Runnable() {

						@Override
						public void run() {
							JOptionPane.showMessageDialog(null,
									"Check has been broken.", "No more CHECK",
									JOptionPane.INFORMATION_MESSAGE);

						}
					});

					thread.start();
					
					return true;
				}
				
				
			}
			
			// See if we can capture the threatning unit
			
			ArrayList<ChessUnit> threatunits = getUnits_that_threaten();
			
			
			
			for (int i = 0; i < threatunits.size(); i++) {
			
				if (loc.equals(threatunits.get(i).current_location)) {
				
				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						JOptionPane.showMessageDialog(null,
								"Check has been broken.", "No more CHECK",
								JOptionPane.INFORMATION_MESSAGE);

					}
				});

				thread.start();
					
					return true;
				}
	
			}
		
			// See if we can block the threatning unit
			
			
			
				Location oldLocation = Location.getLocation(unit.current_location.Row, unit.current_location.Column);
				unit.current_location = Location.getLocation(loc.Row,loc.Column);
				
				if (Check() == false) {
					
				
					Thread thread = new Thread(new Runnable() {
						
						@Override
						public void run() {
							JOptionPane.showMessageDialog(null, "Check has been broken.", "No more CHECK",JOptionPane.INFORMATION_MESSAGE);
							
						}
					});
					
					thread.start();
					unit.current_location = Location.getLocation(oldLocation.Row, oldLocation.Column);
					return true;
				}
				
				unit.current_location = Location.getLocation(oldLocation.Row, oldLocation.Column);
				
			// If nothing works then this move is not possible (will not remove check);
			
			
			return false;
			
		}

		/** returns true if king in check
		 * 
		 */
		
		static boolean Check(){
			
			ArrayList<ChessUnit> roll = null;
			King targetKing = null;
			
			
			if (GameState.turn == Allegiance.white)
			{
				roll = BlackUnitRoll;
				targetKing = GameState.blackTargetKing;
				
			}
			
			if (GameState.turn == Allegiance.black)
			{
				roll = WhiteUnitRoll;
				targetKing = GameState.whiteTargetKing;
			}
			
			
			for (int i = 0; i < roll.size(); i++) {

				roll.get(i).refineMoves();

				for (int j = 0; j < roll.get(i).RefinedMoves.size(); j++) {

					if (roll.get(i).alive == true) {

						if (roll.get(i).RefinedMoves.get(j).equals(targetKing.current_location)) {
							//ChessSquare.ColoriseMoves(roll.get(i));
							return true;

						}

					}
				}
			}

		return false;
			
		}

		/** King getting killed due to move
		 * 
		 * @param unit
		 * @param loc
		 * @return
		 */
		
		static boolean ImpossibleMove_check(ChessUnit unit, Location loc){
			
			Location oldLocation = Location.getLocation(unit.current_location.Row, unit.current_location.Column);
			unit.current_location = Location.getLocation(loc.Row,loc.Column);
			
			ArrayList<ChessUnit> roll = null;
			King targetKing = null;
			
			GameState.switchTurns();
			
			if (GameState.turn == Allegiance.white)
			{
				roll = WhiteUnitRoll;
				targetKing = GameState.whiteTargetKing;
				
			}
			
			if (GameState.turn == Allegiance.black)
			{
				roll = BlackUnitRoll;
				targetKing = GameState.blackTargetKing;
			}
			
			
			for (int i = 0; i < roll.size(); i++) {
	
				roll.get(i).refineMoves();
	
				for (int j = 0; j < roll.get(i).RefinedMoves.size(); j++) {
	
					if (roll.get(i).alive == true) {
	
						if (roll.get(i).RefinedMoves.get(j).equals(
								targetKing.current_location))
							;
						{
							GameState.switchTurns();
							unit.current_location = Location.getLocation(oldLocation.Row, oldLocation.Column);
							return true;
						}
	
					}
	
				}
			}
			
		unit.current_location = Location.getLocation(oldLocation.Row, oldLocation.Column);
		GameState.switchTurns();
		return false;
			
		}
		

		
		
	}
	





