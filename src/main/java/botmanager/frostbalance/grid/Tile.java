package botmanager.frostbalance.grid;

import botmanager.frostbalance.grid.coordinate.Hex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Tile implements Containable<WorldMap>, Container {

    transient WorldMap map;

    ClaimData claimData = new ClaimData(this);

    /**
     * A list of all objects currently on this tile.
     */
    List<TileObject> objects = new ArrayList<>();
    Hex location;

    public Tile(WorldMap map, Hex location) {
        this.map = map;
        this.location = location;
    }

    public Hex getLocation() {
        return location;
    }

    public Tile moveObject(TileObject tileObject, Hex location) {
        Tile tile = map.getTile(location);
        objects.remove(tileObject);
        tile.addObject(tileObject);
        return tile;
    }

    public void addObject(TileObject tileObject) {
        objects.add(tileObject);
    }

    public Collection<TileObject> getObjects() {
        return objects;
    }

    public WorldMap getMap() {
        return map;
    }

    public ClaimData getClaimData() {
        if (claimData == null) {
            claimData = new ClaimData(this);
        }
        return claimData;
    }

    public void removeObject(TileObject tileObject) {
        this.objects.remove(tileObject);
    }

    @Override
    public void setParent(WorldMap parent) {
        this.map = parent;
    }

    @Override
    public void adopt() {
        for (TileObject tileObject: objects) {
            tileObject.setParent(this);
            if (tileObject instanceof Container) ((Container) tileObject).adopt();
        }
        getClaimData().setParent(this);
        claimData.adopt();
    }

    public boolean isEmpty() {
        return claimData.getClaims().isEmpty() && objects.isEmpty();
    }

    public Tile getNeighbor(Hex.Direction direction) {
        return getMap().getTile(getLocation().move(direction));
    }
}
