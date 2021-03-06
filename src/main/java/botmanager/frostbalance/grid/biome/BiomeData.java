package botmanager.frostbalance.grid.biome;

import botmanager.Utilities;
import botmanager.frostbalance.grid.Containable;
import botmanager.frostbalance.grid.Tile;
import botmanager.frostbalance.grid.coordinate.Hex;
import botmanager.frostbalance.resource.RandomId;

public class BiomeData implements Containable<Tile> {

    private static final double OFFSET_SEVERITY = 3.0;

    transient private Tile tile;

    //heat ranges from 0 to 1.
    private transient double temperature = 0;

    //heat offset to make transitions less artificial ranges from -0.1 to 0.1, normalized.
    private transient double temperatureOffset = 0;
    private static final double SHADE_MAGNITUDE = 1.0 / 6;
    private static final double SHADE_SCALE = 0.75;


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
    private static final double RIVER_SCALE = 3;
    private static final int NUM_RIVER_SYSTEMS = 1; //all odd numbers guarantee the player spawns on a river!

    public BiomeData(Tile tile) {

        this.tile = tile;

    }

    private static final double TEMPERATURE_SCALE = 4.0; //changes slower

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

    private static final double HUMIDITY_SCALE = 2.2; //changes fastest

    private double generateHumidity() {
        return simplexTile(tile.getLocation().drawX() / Hex.X_SCALE / HUMIDITY_SCALE,
                tile.getLocation().drawY() / Hex.Y_SCALE / HUMIDITY_SCALE,
                1);
    }


    private double generateElevationOffset() {
        return (Math.pow(simplexTile(tile.getLocation().drawX() / Hex.X_SCALE / BUMP_SCALE,
                tile.getLocation().drawY() / Hex.Y_SCALE / BUMP_SCALE,
                2) * 2 - 1, OFFSET_SEVERITY) + 1) / 2;
    }


    private double generateHeatOffset() {
        return (Math.pow(simplexTile(tile.getLocation().drawX() / Hex.X_SCALE / SHADE_SCALE,
                tile.getLocation().drawY() / Hex.Y_SCALE / SHADE_SCALE,
                3) * 2 - 1, OFFSET_SEVERITY) + 1) / 2;
    }

    private double generateHumidityOffset() {
        return (Math.pow(simplexTile(tile.getLocation().drawX() / Hex.X_SCALE / WEATHER_SCALE,
                tile.getLocation().drawY() / Hex.Y_SCALE / WEATHER_SCALE,
                4) * 2 - 1, OFFSET_SEVERITY) + 1) / 2;
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

    private double simplexTile(double coord1, double coord2, long generatorSeed) {
        OpenSimplexNoise noise = new OpenSimplexNoise(Utilities.combineSeed(RandomId.TILE_GENERATOR, generatorSeed, tile.getMap().getSeed()));
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
        return ((baseHumidity == 0 ? baseHumidity = generateHumidity() : baseHumidity)
                 * (1 - WEATHER_MAGNITUDE))
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
    public double intemperance() {
        return Math.abs(getTemperature() * 2 - 1);
    }

    @Override
    public void setParent(Tile parent) {
        this.tile = parent;
    }

    public Biome getBiome() {

        Biome baseBiome = Biome.from(
                ElevationClass.from(getElevation()),
                TemperatureClass.from(getTemperature()),
                HumidityClass.from(getHumidity()));

        if (isRiver() && baseBiome.getElevation() != ElevationClass.BASIN) return Biome.RIVER;
        if (baseBiome.getEnvironment() == Biome.Environment.SEA && getElevation() > 0.30) {
            return Biome.COAST;
        }

        return baseBiome;

    }
}
