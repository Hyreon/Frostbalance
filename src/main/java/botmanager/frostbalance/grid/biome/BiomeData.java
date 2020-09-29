package botmanager.frostbalance.grid.biome;

import botmanager.frostbalance.grid.Containable;
import botmanager.frostbalance.grid.Tile;
import botmanager.frostbalance.grid.coordinate.Hex;

public class BiomeData implements Containable<Tile> {

    private static final double OFFSET_SEVERITY = 3.0;

    transient private Tile tile;

    //heat ranges from 0 to 1.
    private transient double temperature = 0;

    //heat offset to make transitions less artificial ranges from -0.1 to 0.1, normalized.
    private transient double temperatureOffset = 0;
    private static final double SHADE_MAGNITUDE = 1.0 / 6;
    private static final double SHADE_SCALE = 1.0;


    //depth ranges from 0 to 1.
    private transient double elevation = 0;

    //elevation offset (for lakes, islands, hills and valleys) ranges from -0.2 to 0.2, normalized.
    private transient double elevationOffset = 0;
    private static final double BUMP_MAGNITUDE = 1.0 / 6;
    private static final double BUMP_SCALE = 1.0;

    //humidity ranges from 0 to 1.
    private transient double baseHumidity = 0;

    //humidity offset to make transitions less artificial ranges from -0.4 to 0.4, normalized.
    private transient double humidityOffset = 0;
    private static final double WEATHER_MAGNITUDE = 2.0 / 7;
    private static final double WEATHER_SCALE = 0.5;

    //river property from 0 to 1 indicates how close this is to the river map.
    private transient double river = 0;
    private static final double RIVER_SCALE = 5;
    private static final int NUM_RIVER_SYSTEMS = 1; //all odd numbers guarantee the player spawns on a river!

    public BiomeData(Tile tile) {

        this.tile = tile;

    }

    private static final double TEMPERATURE_SCALE = 3.50; //changes slower

    private double generateHeat() {
        return simplexTile(tile.getLocation().drawX() / Hex.X_SCALE / TEMPERATURE_SCALE,
                tile.getLocation().drawY() / Hex.Y_SCALE / TEMPERATURE_SCALE,
                -1);
    }
    private static final double ELEVATION_SCALE = 10.0; //changes much slower

    private double generateElevation() {
        return simplexTile(tile.getLocation().drawX() / Hex.X_SCALE / ELEVATION_SCALE,
                tile.getLocation().drawY() / Hex.Y_SCALE / ELEVATION_SCALE,
                0);
    }

    private static final double HUMIDITY_SCALE = 1.8; //changes fastest

    private double generateHumidity() {
        return simplexTile(tile.getLocation().drawX() / Hex.X_SCALE / HUMIDITY_SCALE,
                tile.getLocation().drawY() / Hex.Y_SCALE / HUMIDITY_SCALE,
                1);
    }


    private double generateElevationOffset() {
        return Math.pow(simplexTile(tile.getLocation().drawX() / Hex.X_SCALE / BUMP_SCALE,
                tile.getLocation().drawY() / Hex.Y_SCALE / BUMP_SCALE,
                2) * 2 - 1, OFFSET_SEVERITY) + 1.0 / 2;
    }


    private double generateHeatOffset() {
        return Math.pow(simplexTile(tile.getLocation().drawX() / Hex.X_SCALE / SHADE_SCALE,
                tile.getLocation().drawY() / Hex.Y_SCALE / SHADE_SCALE,
                3) * 2 - 1, OFFSET_SEVERITY) + 1.0 / 2;
    }

    private double generateHumidityOffset() {
        return Math.pow(simplexTile(tile.getLocation().drawX() / Hex.X_SCALE / WEATHER_SCALE,
                tile.getLocation().drawY() / Hex.Y_SCALE / WEATHER_SCALE,
                4) * 2 - 1, OFFSET_SEVERITY) + 1.0 / 2;
    }

    private double generateRiverMap() {
        double riverIndex = simplexTile(tile.getLocation().drawX() / Hex.X_SCALE / RIVER_SCALE,
                tile.getLocation().drawY() / Hex.Y_SCALE / RIVER_SCALE,
                4);
        double distanceToClosestRiver = 1;
        for (int i = 0; i < NUM_RIVER_SYSTEMS; i++) {
            distanceToClosestRiver = Math.min(Math.abs((i + 0.5) / NUM_RIVER_SYSTEMS - riverIndex) * NUM_RIVER_SYSTEMS, distanceToClosestRiver);
        }
        return 1 - distanceToClosestRiver;
    }

    private double simplexTile(double coord1, double coord2, long seed) {
        OpenSimplexNoise noise = new OpenSimplexNoise(seed);
        double ZOOM_AMOUNT = 1.0 / 16;
        return (noise.eval(coord1 * ZOOM_AMOUNT, coord2 * ZOOM_AMOUNT) + 1)
                / 2.0;
    }

    public double getTemperatureOffset() {
        return temperatureOffset == 0 ? temperatureOffset = generateHeatOffset() : temperatureOffset;
    }

    public double getHumidityOffset() {
        return humidityOffset == 0 ? humidityOffset = generateHumidityOffset() : humidityOffset;
    }

    public double getBaseTemperature() {
        return temperature == 0 ? temperature = generateHeat() : temperature;
    }

    public double getTemperature() {
        return getBaseTemperature() * (1 - SHADE_MAGNITUDE) + getTemperatureOffset() * SHADE_MAGNITUDE;
    }

    public double getBaseElevation() {
        return elevation == 0 ? elevation = generateElevation() : elevation;
    }

    public double getElevationOffset() {
        return elevationOffset == 0 ? elevationOffset = generateElevationOffset() : elevationOffset;
    }

    public double getElevation() {
        return getBaseElevation() * (1 - BUMP_MAGNITUDE) + getElevationOffset() * BUMP_MAGNITUDE;
    }

    public double getHumidity() {
        return ((baseHumidity == 0 ? baseHumidity = generateHumidity() : baseHumidity) * 0.7
                + (1 - intemperance()) * 0.15 //evaporation
                - (getElevation()) * 0.15) //water falloff
                 * (1 - WEATHER_MAGNITUDE)
                + getHumidityOffset() * WEATHER_MAGNITUDE;
    }

    public boolean isRiver() {
        return (tile.getLocation().minimumDistance(Hex.origin()) > 6) &&
                (river = river == 0 ? generateRiverMap() : river) >= 1 - 0.05 / RIVER_SCALE;
    }

    /**
     * Gets how far the temperature is from a moderate 0.5.
     * @return A value between 1 and 0; 0 is temperate, 1 is intemperate.
     */
    private double intemperance() {
        return Math.abs(getTemperature() * 2 - 1);
    }

    @Override
    public void setParent(Tile parent) {
        this.tile = parent;
    }

    public Biome getBiome() {
        if (getElevation() > 0.85) {
            if (getTemperature() > 0.7) {
                return Biome.MESA;
            } else if (getTemperature() > 0.3) {
                return Biome.ROCKY;
            } else if (getHumidity() > 0.5) {
                return Biome.ALP;
            } else {
                return Biome.SNOW_PEAK;
            }
        } else if (getElevation() > 0.35) {

            if (isRiver()) return Biome.RIVER;

            //special cases
            //if (getBaseElevation() < 0.2) {
            //    return Biome.ISLAND;
            //}
            //if (getBaseElevation() < 0.3) {
            //    return Biome.BEACH;
            //}

            if (getTemperature() < 0.3) {
                if (getHumidity() < 0.25) {
                    return Biome.TUNDRA;
                } else if (getHumidity() < 0.5) {
                    return Biome.SNOW_FIELD;
                } else if (getHumidity() < 0.75) {
                    return Biome.TAIGA;
                } else {
                    return Biome.MARSH;
                }
            } else if (getTemperature() < 0.7) {
                if (getHumidity() < 0.25) {
                    return Biome.TEMPERATE_DESERT;
                } else if (getHumidity() < 0.5) {
                    return Biome.GRASSLANDS;
                } else if (getHumidity() < 0.75) {
                    return Biome.FOREST;
                } else {
                    return Biome.SWAMP;
                }
            } else {
                if (getHumidity() < 0.25) {
                    return Biome.DESERT;
                } else if (getHumidity() < 0.5) {
                    return Biome.PRAIRIE;
                } else if (getHumidity() < 0.75) {
                    return Biome.SAVANNA;
                } else {
                    return Biome.JUNGLE;
                }
            }
        } else {
            if (getTemperature() < 0.2) {
                return Biome.ICE;
            } else if (getHumidity() > 0.8 && getTemperature() > 0.6) {
                return Biome.STORMY_SEA;
            } else {
                if (getElevation() > 0.30) {
                    return Biome.COAST;
                }
                return Biome.SEA;
            }
        }
    }
}
