package botmanager.frostbalance.grid;

import botmanager.frostbalance.grid.coordinate.Hex;

import java.awt.image.BufferedImage;

public abstract class TileObject extends TileData implements Renderable {

    transient private BufferedImage cachedImage;
    transient private long cachedImageDate;

    protected TileObject(Tile tile) {
        super(tile);
    }

    public Tile getTile() {
        return tile;
    }

    public Hex getLocation() {
        return tile.getLocation();
    }

    public WorldMap getMap() {
        return tile.getMap();
    }

    @Override
    public long getCachedImageDate() {
        return cachedImageDate;
    }

    @Override
    public void setCachedImageDate(long cachedImageDate) {
        this.cachedImageDate = cachedImageDate;
    }

    @Override
    public BufferedImage getCachedImage() {
        return cachedImage;
    }

    @Override
    public void setCachedImage(BufferedImage cachedImage) {
        this.cachedImage = cachedImage;
    }
}
