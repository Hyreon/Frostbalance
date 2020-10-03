package botmanager.frostbalance.grid;

import botmanager.frostbalance.grid.coordinate.Hex;

public abstract class Mobile extends TileObject {

    public void setLocation(Hex hex) {
        setTile(getTile().moveObject(this, hex));
    }

    protected void setTile(Tile tile) {
        this.tile = tile;
    }

    protected Mobile(Tile tile) {
        super(tile);
        tile.addObject(this);
    }

}
