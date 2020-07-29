package botmanager.frostbalance.grid;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public abstract class TileObject extends TileData {

    transient private BufferedImage cachedImage;

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

    public BufferedImage getImage() throws IOException {
        if (cachedImage != null) return cachedImage;
        else {
            cachedImage = ImageIO.read(getRender());
            return cachedImage;
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
