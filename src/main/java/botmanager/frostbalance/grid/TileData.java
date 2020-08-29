package botmanager.frostbalance.grid;

public abstract class TileData implements Containable<Tile> {

    transient protected Tile tile;

    public TileData(Tile tile) {
        this.tile = tile;
    }

    public Tile getTile() {
        return tile;
    }

    @Override
    public void setParent(Tile parent) {
        tile = parent;
    }

}
