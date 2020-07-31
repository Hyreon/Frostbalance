package botmanager.frostbalance.grid;

import botmanager.Utilities;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public abstract class TileObject extends TileData {

    transient private BufferedImage cachedImage;
    transient private long cachedImageDate;

    protected TileObject(Tile tile) {
        super(tile);
        tile.addObject(this);
    }

    public Tile getTile() {
        return tile;
    }

    public void setLocation(Hex hex) {
        setTile(getTile().moveObject(this, hex));
    }

    protected void setTile(Tile tile) {
        this.tile = tile;
    }

    public boolean isImageCacheValid() {
        return cachedImage != null && cachedImageDate >= Utilities.todayAsLong();
    }

    protected void invalidateCache() {
        cachedImage = null;
    }

    public BufferedImage getImage() throws IOException {
        if (isImageCacheValid()) return cachedImage;
        else {
            InputStream renderStream = getRender();
            if (renderStream == null) {
                return null;
            } else {
                cachedImage = ImageIO.read(getRender());
                cachedImageDate = Utilities.todayAsLong();
                return cachedImage;
            }
        }
    }

    public abstract InputStream getRender();

    public Hex getLocation() {
        return tile.getLocation();
    }

    public WorldMap getMap() {
        return tile.getMap();
    }

}
