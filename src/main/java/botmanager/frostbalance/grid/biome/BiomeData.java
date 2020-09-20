package botmanager.frostbalance.grid.biome;

import botmanager.frostbalance.grid.Containable;
import botmanager.frostbalance.grid.Tile;
import botmanager.frostbalance.grid.coordinate.Hex;

public class BiomeData implements Containable<Tile> {

    transient private Tile tile;

    //heat ranges from 0 to 1. it is dependent on x and y.
    transient double temperature = 0;

    //depth ranges from 0 to 1. it is dependent on y and z.
    transient double elevation = 0;

    //humidity ranges from 0 to 1. it is dependent on z and x.
    transient double humidity = 0;

    public BiomeData(Tile tile) {

        this.tile = tile;

        temperature = generateHeat();
        elevation = generateElevation();
        humidity = generateHumidity();

    }

    private static final double TEMPERATURE_SCALE = 2.0; //changes a little slower

    private double generateHeat() {
        return simplexTile(tile.getLocation().drawX() / Hex.X_SCALE / TEMPERATURE_SCALE,
                tile.getLocation().drawY() / Hex.Y_SCALE / TEMPERATURE_SCALE,
                -1, 500);
    }

    private static final double ELEVATION_SCALE = 10.0; //changes much slower

    private double generateElevation() {
        return simplexTile(tile.getLocation().drawX() / Hex.X_SCALE / ELEVATION_SCALE,
                tile.getLocation().drawY() / Hex.Y_SCALE / ELEVATION_SCALE,
                0, -500);
    }

    private static final double HUMIDITY_SCALE = 2.0; //changes fastest

    private double generateHumidity() {
        return simplexTile(tile.getLocation().drawX() / Hex.X_SCALE / HUMIDITY_SCALE,
                tile.getLocation().drawY() / Hex.Y_SCALE / HUMIDITY_SCALE,
                1, 0);
    }


    private double simplexTile(double coord1, double coord2, long seed, double offset) {
        OpenSimplexNoise noise = new OpenSimplexNoise(seed);
        double ZOOM_AMOUNT = 1.0 / 8;
        return (noise.eval(coord1 * ZOOM_AMOUNT + offset, coord2 * ZOOM_AMOUNT + offset) + 1)
                / 2.0;
    }

    public double getTemperature() {
        return temperature == 0 ? temperature = generateHeat() : temperature;
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

    public Biome getBiome() {
        if (getElevation() > 0.8) {
            if (temperature > 0.8) {
                return Biome.MESA;
            } else if (temperature > 0.3) {
                return Biome.MOUNTAIN;
            } else {
                return Biome.SNOW_MOUNTAIN;
            }
        } else if (getElevation() > 0.3) {
            if (humidity < 0.2) {
                if (temperature > 0.7) {
                    return Biome.ARID_DESERT;
                } else if (temperature > 0.2) {
                    return Biome.DESERT;
                } else {
                    return Biome.TUNDRA;
                }
            } else if (humidity < 0.5) {
                if (temperature > 0.5) {
                    return Biome.SHRUBLANDS;
                } else {
                    return Biome.GRASSLANDS;
                }
            } else if (humidity < 0.8) {
                if (temperature < 0.3) {
                    return Biome.TAIGA;
                } else if (temperature < 0.8) {
                    return Biome.FOREST;
                } else return Biome.SAVANNA;
            } else {
                if (temperature < 0.5) {
                    return Biome.SWAMP;
                } else return Biome.JUNGLE;
            }
        } else {
            if (getTemperature() < 0.2) {
                return Biome.ICE;
            } else if (getHumidity() > 0.8 && getTemperature() > 0.6) {
                return Biome.STORMY_SEA;
            } else {
                return Biome.SEA;
            }
        }
    }
}
