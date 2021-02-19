package botmanager.frostbalance.resource;

import botmanager.frostbalance.grid.biome.Biome;
import botmanager.frostbalance.grid.biome.ElevationClass;
import botmanager.frostbalance.grid.biome.HumidityClass;
import botmanager.frostbalance.grid.biome.TemperatureClass;
import botmanager.frostbalance.grid.building.Gatherer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class DepositType {

    public String name;

    public ItemType itemType;

    HashMap<Biome, Integer> points = new HashMap<>(); //odds of finding this resource
    HashMap<HumidityClass, Integer> humidityModifiers = new HashMap<>();
    HashMap<TemperatureClass, Integer> temperatureModifiers = new HashMap<>();
    HashMap<ElevationClass, Integer> elevationModifiers = new HashMap<>();

    Gatherer.Method gatherer;

    public DepositType(@NotNull String name, ItemType itemType, Gatherer.Method method, HashMap<Biome, Integer> points,
                       HashMap<ElevationClass, Integer> elevationModifiers, HashMap<TemperatureClass, Integer> temperatureModifiers, HashMap<HumidityClass, Integer> humidityModifiers) {
        this.name = name;
        if (points != null) {
            for (Biome biome : points.keySet()) {
                this.points.put(biome, points.get(biome));
            }
        }
        if (elevationModifiers != null)
        this.elevationModifiers = elevationModifiers;
        if (temperatureModifiers != null)
        this.temperatureModifiers = temperatureModifiers;
        if (humidityModifiers != null)
        this.humidityModifiers = humidityModifiers;
        gatherer = method;
        this.itemType = itemType;
    }

    //TODO change the biome format to allow for the use of modifiers.
    public int pointsIn(Biome biome) {
        return Math.max(points.getOrDefault(biome, 0), 0)
                + humidityModifiers.getOrDefault(biome.getHumidity(), 0)
                + temperatureModifiers.getOrDefault(biome.getTemperature(), 0)
                + elevationModifiers.getOrDefault(biome.getElevation(), 0);
    }

    public String getId() {
        return name;
    }

    public String toString() {
        return getId();
    }
}
