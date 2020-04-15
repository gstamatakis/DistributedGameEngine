public class Knight extends ChessUnit {

    Knight(Location location, Allegiance allegiance) {
        super(location, allegiance);

        this.movement_Type = Movement_Type.SingleHop;

        this.RawMoves.add(new Location(+2, +1));
        this.RawMoves.add(new Location(+1, +2));
        this.RawMoves.add(new Location(-1, +2));
        this.RawMoves.add(new Location(-2, +1));
        this.RawMoves.add(new Location(-2, -1));
        this.RawMoves.add(new Location(-1, -2));
        this.RawMoves.add(new Location(+1, -2));
        this.RawMoves.add(new Location(+2, -1));

        if (allegiance == Allegiance.white) {

            this.graphicPath = "GameArt/White/Knight.png";

        } else if (allegiance == Allegiance.black) {

            this.graphicPath = "GameArt/Black/Knight.png";

        }

        // Place Image on GUI
        GUI_ChessPanel.placeUnit(location, this);
    }


}
