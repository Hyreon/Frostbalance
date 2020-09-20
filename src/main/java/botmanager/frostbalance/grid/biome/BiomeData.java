package botmanager.frostbalance.grid.biome;

import botmanager.frostbalance.grid.Containable;
import botmanager.frostbalance.grid.Tile;

public class BiomeData implements Containable<Tile> {

    transient private Tile tile;

    //heat ranges from 0 to 1. it is dependent on x and y.
    transient double heat = 0;

    //depth ranges from 0 to 1. it is dependent on y and z.
    transient double elevation = 0;

    //humidity ranges from 0 to 1. it is dependent on z and x.
    transient double humidity = 0;

    public BiomeData(Tile tile) {

        this.tile = tile;

        heat = generateHeat();
        elevation = generateElevation();
        humidity = generateHumidity();

    }

    private double generateHeat() {
        return simplexTile(tile.getLocation().drawX(), tile.getLocation().drawY(), BiomeSeed.HEAT);
    }

    private double generateElevation() {
        return simplexTile(tile.getLocation().drawX(), tile.getLocation().drawY(), BiomeSeed.DEPTH);
    }

    private double generateHumidity() {
        return simplexTile(tile.getLocation().drawX(), tile.getLocation().drawY(), BiomeSeed.HUMIDITY);
    }


    private double simplexTile(double coord1, double coord2, BiomeSeed biomeSeed) {
        OpenSimplexNoise noise = new OpenSimplexNoise(biomeSeed.seed);
        double ZOOM_AMOUNT = 1.0 / 800;
        double OFFSET_AMOUNT = 500;
        return (noise.eval(coord1 * ZOOM_AMOUNT + 500, coord2 * ZOOM_AMOUNT + 500) + 1)
                / 2.0;
    }

    public double getHeat() {
        return heat == 0 ? heat = generateHeat() : heat;
    }

    public double getElevation() {
        return elevation;
    }

    public double getHumidity() {
        return humidity;
    }

    @Override
    public void setParent(Tile parent) {
        this.tile = parent;
    }
}
