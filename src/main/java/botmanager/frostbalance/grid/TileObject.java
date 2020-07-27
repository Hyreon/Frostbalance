package botmanager.frostbalance.grid;

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

    public abstract URL getRender();

}
