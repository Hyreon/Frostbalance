package botmanager.frostbalance.grid;

public abstract class TileData implements Containable<Tile> {

    transient protected Tile tile;

    public TileData(Tile tile) {
        this.tile = tile;
    }

    public Tile getTile() {
        return tile;
    }

    public void setInitialTile(Tile tile) {
        if (this.tile != null) {
            throw new IllegalArgumentException("Tile is already set!");
        } else {
            this.tile = tile;
        }
    }

}
