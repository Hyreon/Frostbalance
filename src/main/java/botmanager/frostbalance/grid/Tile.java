package botmanager.frostbalance.grid;

import botmanager.frostbalance.Nation;
import botmanager.frostbalance.grid.biome.*;
import botmanager.frostbalance.grid.coordinate.Hex;
import botmanager.frostbalance.resource.ResourceData;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Tile implements Containable<WorldMap>, Container {

    transient WorldMap map;

    ClaimData claimData = new ClaimData(this);

    ResourceData resourceData = new ResourceData(this);

    transient BiomeData biomeData = new BiomeData(this);

    /**
     * A list of all objects currently on this tile.
     */
    List<Mobile> objects = new ArrayList<>();
    Hex location;

    BuildingData buildingData;

    public Tile(WorldMap map, Hex location) {
        this.map = map;
        this.location = location;
    }

    public Hex getLocation() {
        return location;
    }

    public Tile moveObject(Mobile mob, Hex location) {
        Tile tile = map.getTile(location);
        objects.remove(mob);
        tile.addObject(mob);
        return tile;
    }

    public void addObject(Mobile mob) {
        objects.add(mob);
    }

    public Collection<Mobile> getMobs() {
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

    public ResourceData getResourceData() {
        if (resourceData == null) {
            resourceData = new ResourceData(this);
        }
        return resourceData;
    }

    public BuildingData getBuildingData() {
        if (buildingData == null) {
            buildingData = new BuildingData(this);
        }
        return buildingData;
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
        while (objects.contains(null)) {
            objects.remove(null);
        }
        for (TileObject tileObject: objects) {
            tileObject.setParent(this);
            if (tileObject instanceof Container) ((Container) tileObject).adopt();
        }
        getBuildingData().setParent(this);
        getClaimData().setParent(this);
        getResourceData().setParent(this);
        claimData.adopt();
        buildingData.adopt();
        resourceData.adopt();
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

    public HumidityClass getHumidityClass() {
        return HumidityClass.from(biomeData.getHumidity());
    }

    public TemperatureClass getTemperatureClass() {
        return TemperatureClass.from(biomeData.getTemperature());
    }

    public ElevationClass getElevationClass() {
        return ElevationClass.from(biomeData.getElevation());
    }
}
