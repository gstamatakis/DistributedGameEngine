import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
/** This class handles the SWING/GUI coponents of the chess board.
 * 	
 * 
 * @author 13020348
 *
 */
class GUI_ChessPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	static ChessSquare[][] chessSquare_Array;
	
	
	GUI_ChessPanel() {
 
		super();
		this.setLayout(new GridLayout(8, 8));
		chessSquare_Array = new ChessSquare[9][9];
		
		
		for (int i = 8; i >= 1; i--) 
		{
			for (int j = 1; j <= 8; j++) 
			{
				ChessSquare newSquare = new ChessSquare(new Location(i, j));
				chessSquare_Array[newSquare.Location.Row][newSquare.Location.Column] = newSquare;
				newSquare.ResetSquareColor();
				this.add(newSquare);

			}
		}

	}
	
	/** Places a unit on the given Location of the GUI
	 * 
	 * @param location
	 * @param unit
	 */
 
	static void placeUnit(Location location, ChessUnit unit) {
				
		chessSquare_Array[location.Row][location.Column].setUnit(unit.graphicPath);
		
	}

	/** Removes the unit places on the given location of the GUI
	 * 
	 * @param location
	 */
	
	static void removeUnit(Location location) {
		
		chessSquare_Array[location.Row][location.Column].setIcon(null);
		chessSquare_Array[location.Row][location.Column].theImageIcon = null;
		chessSquare_Array[location.Row][location.Column].repaint();
		
	}


}

/** Chess Squares are the boxes on which Chess Units are located and on which
 *  they move and capture
 * 
 * @author 13020348
 *
 */

class ChessSquare extends JLabel implements MouseListener {
	
	// Various colors used by the chess squares ... in normal mode and
	// while hilighting
	
	static Color blue = new Color(0,128,192);
	static Color red = new Color(190,57,1);
	static Color white = new Color(224, 224, 224);
	static Color black = new Color(64, 64, 64);
	static Color green = new Color(97, 255, 94);
	
	private static final long serialVersionUID = 1L;

	// The Unit Graphic image accociated with the square.
	
	ImageIcon theImageIcon = null;
	
	// The Location of the square (row-column pair)
	
	Location Location;

	
	ChessSquare(Location location) {

		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
		this.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		
		this.setOpaque(true);
		this.addMouseListener(this);
		Location = location;

	}
	

	/** Dynamically Resizes the unit Graphics to the size of the window/Jframe.
	 * 
	 */
	
    @Override
    public void paintComponent (Graphics g) {
        super.paintComponent (g);
        if (theImageIcon != null){
        		g.drawImage (theImageIcon.getImage(), 0, 0, getWidth (), getHeight (), null);
        		
        }
    }

	
    /** Adds the given graphic image as the icon of the square
     * 
     * @param graphicImage
     */
    
	void setUnit(String graphicImage){
		
		theImageIcon = new ImageIcon(graphicImage);
		this.repaint();
	}


	/** Hilights the squares where the unit can move to.
	 * 
	 * @param unit
	 */
	
	static void ColoriseMoves(ChessUnit unit) {
		
		unit.refineMoves();
		GUI_ChessPanel.chessSquare_Array[unit.current_location.Row][unit.current_location.Column].setBackground(green);
		
		for (int i = 0; i < unit.RefinedMoves.size(); i ++){
			
			Location loc = unit.RefinedMoves.get(i);
			
			if (loc.scoutLocation() == Square_Occupancy.Empty)				
				GUI_ChessPanel.chessSquare_Array[loc.Row][loc.Column].setBackground(blue);
			else
				GUI_ChessPanel.chessSquare_Array[loc.Row][loc.Column].setBackground(red);
							
		}

		
		
	}
	
	/** Resets the color of the sqaure according to the normal
	 *  (non-hilighted) coloration of the board
	 * 
	 */
	
	void ResetSquareColor() {

		int row = this.Location.Row;
		int column = this.Location.Column;

		boolean column_is_even = false;
		boolean row_is_even = false;

		if (row % 2 == 0)
			row_is_even = true;
		if (column % 2 == 0)
			column_is_even = true;

		if (row_is_even) 
		{
			if (column_is_even)
				this.setBackground(white);
			else 
				this.setBackground(black);
		}

		else if (!row_is_even) 
		{
			if (column_is_even){
				this.setBackground(black);}
			else 
				this.setBackground(white);
		}

	}


	/** Click on a square
	 * 
	 */
	
	@Override
	public void mouseClicked(MouseEvent arg0) {

		WorkerThread.doWork(Location);

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {}

	@Override
	public void mouseReleased(MouseEvent arg0) {}

}


