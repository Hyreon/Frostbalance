package botmanager.frostbalance.grid;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Tile implements Containable<WorldMap>, Container<TileData> {

    transient WorldMap map;

    ClaimData claimData;

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

    @Override
    public TileData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        TileData tileData = context.deserialize(json, TileData.class);
        tileData.tile = this;
        return tileData;
    }

    public void removeObject(TileObject tileObject) {
        this.objects.remove(tileObject);
    }
}
