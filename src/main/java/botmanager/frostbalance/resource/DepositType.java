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

    HashMap<Biome, Double> points = new HashMap<>(); //odds of finding this resource
    HashMap<HumidityClass, Double> humidityModifiers = new HashMap<>();
    HashMap<TemperatureClass, Double> temperatureModifiers = new HashMap<>();
    HashMap<ElevationClass, Double> elevationModifiers = new HashMap<>();

    Gatherer.Method gatherMethod;

    public DepositType(@NotNull String name, ItemType itemType, Gatherer.Method method, HashMap<Biome, Double> points,
                       HashMap<ElevationClass, Double> elevationModifiers, HashMap<TemperatureClass, Double> temperatureModifiers, HashMap<HumidityClass, Double> humidityModifiers) {
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
        gatherMethod = method;
        this.itemType = itemType;
    }

    //TODO change the biome format to allow for the use of modifiers.
    public double pointsIn(Biome biome) {
        return Math.max(points.getOrDefault(biome, 0.0), 0)
                + humidityModifiers.getOrDefault(biome.getHumidity(), 0.0)
                + temperatureModifiers.getOrDefault(biome.getTemperature(), 0.0)
                + elevationModifiers.getOrDefault(biome.getElevation(), 0.0);
    }

    public String getId() {
        return name;
    }

    public String toString() {
        return getId();
    }
}
