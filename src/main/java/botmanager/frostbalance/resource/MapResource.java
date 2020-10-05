package botmanager.frostbalance.resource;

import botmanager.frostbalance.grid.biome.Biome;
import botmanager.frostbalance.grid.biome.ElevationClass;
import botmanager.frostbalance.grid.biome.HumidityClass;
import botmanager.frostbalance.grid.biome.TemperatureClass;
import botmanager.frostbalance.grid.building.Gatherer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class MapResource {

    public String name;

    public ItemType itemType;

    HashMap<Biome, Integer> points = new HashMap<>(); //odds of finding this resource
    HashMap<HumidityClass, Integer> humidityModifiers = new HashMap<>();
    HashMap<TemperatureClass, Integer> temperatureModifiers = new HashMap<>();
    HashMap<ElevationClass, Integer> elevationModifiers = new HashMap<>();

    Gatherer.Method gatherer;

    //TODO replace this debug method with a proper method
    public MapResource(@NotNull String name) {
        this.name = name;
        for (Biome biome : Biome.values()) {
            points.put(biome, 9);
        }
        gatherer = Gatherer.Method.PLANTATION;
    }

    public int pointsIn(Biome biome) {
        return Math.max(points.getOrDefault(biome, 0), 0);
    }

    public String getId() {
        return name;
    }
}
