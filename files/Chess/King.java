class King extends ChessUnit {

    King(Location location, Allegiance allegiance) {
        super(location, allegiance);


        this.movement_Type = Movement_Type.SingleHop;


        this.RawMoves.add(new Location(+1, 0));
        this.RawMoves.add(new Location(-1, 0));
        this.RawMoves.add(new Location(0, +1));
        this.RawMoves.add(new Location(0, -1));
        this.RawMoves.add(new Location(+1, +1));
        this.RawMoves.add(new Location(-1, -1));
        this.RawMoves.add(new Location(+1, -1));
        this.RawMoves.add(new Location(-1, +1));

        if (allegiance == Allegiance.white) {

            this.graphicPath = "GameArt/White/King.png";
            GameState.blackTargetKing = this;
        } else if (allegiance == Allegiance.black) {

            this.graphicPath = "GameArt/Black/King.png";
            GameState.whiteTargetKing = this;
        }

        // Place Image on GUI
        GUI_ChessPanel.placeUnit(location, this);

    }

}
