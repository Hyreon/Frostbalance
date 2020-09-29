package botmanager.frostbalance.grid;

import botmanager.frostbalance.Nation;
import botmanager.frostbalance.grid.biome.Biome;
import botmanager.frostbalance.grid.biome.BiomeData;
import botmanager.frostbalance.grid.coordinate.Hex;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Tile implements Containable<WorldMap>, Container {

    transient WorldMap map;

    ClaimData claimData = new ClaimData(this);

    transient BiomeData biomeData = new BiomeData(this);

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
        this.biomeData = new BiomeData(this);
    }

    @Override
    public void adopt() {
        for (TileObject tileObject: objects) {
            tileObject.setParent(this);
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

    private static final int BASE_COLOR = 64;

    public Color getNaiveBiomeColor() {
        return new Color((int) (biomeData.getTemperature() * 255),
                (int) (biomeData.getElevation() * 255),
                (int) (biomeData.getHumidity() * 255));
    }

    public Color getBiomeColor() {
        return biomeData.getBiome().getColor();
    }

    public Color getPoliticalColor() {
        Nation owningNation = getClaimData().getOwningNation();
        if (owningNation != null && getMap().getHighestLevelClaim() != null) {
            double intensity = getClaimData().getClaimLevel() / getMap().getHighestLevelClaim().getClaimLevel();
            Color nationColor = owningNation.getColor();
            return new Color(
                    (int) (BASE_COLOR * (1 - intensity) + (nationColor.getRed() * intensity)),
                    (int) (BASE_COLOR * (1 - intensity) + (nationColor.getGreen() * intensity)),
                    (int) (BASE_COLOR * (1 - intensity) + (nationColor.getBlue() * intensity))
            );
        } else {
            return new Color(BASE_COLOR, BASE_COLOR, BASE_COLOR);
        }
    }

    public Color getPoliticalBorderColor() {
        Nation owningNation = getClaimData().getOwningNation();
        if (owningNation != null && getMap().getHighestLevelClaim() != null) {
            return owningNation.getColor();
        } else {
            Color baseColor = getBiomeColor();
            return new Color(baseColor.getRed() * 3 / 4, baseColor.getGreen() * 3 / 4, baseColor.getBlue() * 3 / 4);
        }
    }

    public Biome getBiome() {
        return biomeData.getBiome();
    }
}
