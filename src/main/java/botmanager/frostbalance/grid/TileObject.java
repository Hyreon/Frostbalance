package botmanager.frostbalance.grid;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public abstract class TileObject {

    /**
     * The map this item is connected to.
     */
    protected WorldMap map;

    /**
     * The location on the map's grid.
     */
    private Hex location;

    private BufferedImage cachedImage;

    protected TileObject(WorldMap map, Hex location) {
        this.map = map;
        this.location = location;
        map.getTile(location).addObject(this);
    }

    public WorldMap getMap() {
        return map;
    }

    public Hex getLocation() {
        return location;
    }

    public void setLocation(Hex hex) {
        map.getTile(location).moveObject(this, hex);
        this.location = hex;
    }

    public BufferedImage getImage() throws IOException {
        if (cachedImage != null) return cachedImage;
        else return ImageIO.read(getRender());
    }

    public abstract URL getRender();

}
