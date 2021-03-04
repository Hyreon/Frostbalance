package botmanager.frostbalance.resource;

import botmanager.frostbalance.grid.biome.*;
import botmanager.frostbalance.grid.building.Gatherer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class DepositType {

    public String name;

    public ItemType itemType;

    HashMap<Biome, Double> points = new HashMap<>(); //odds of finding this resource

    Gatherer.Method gatherMethod;

    /**
     * Creates a new deposit type with the given parameters.
     * @param name The name of this deposit. Also used as an id.
     * @param itemType The single item this deposit will yield.
     * @param method The sort of building used to exploit this deposit. Affects how long-term these deposits can be.
     * @param points Points by biome group. Note that this, along with all other modifiers, is reduced into a single map to each biome point values.
     * @param elevationModifiers
     * @param temperatureModifiers
     * @param humidityModifiers
     */
    public DepositType(@NotNull String name, ItemType itemType, Gatherer.Method method, HashMap<BiomeGroup, Double> points,
                       HashMap<ElevationClass, Double> elevationModifiers, HashMap<TemperatureClass, Double> temperatureModifiers, HashMap<HumidityClass, Double> humidityModifiers) {
        this.name = name;
        gatherMethod = method;
        this.itemType = itemType;

        if (itemType == null) {
            System.err.println("Missing an item type for deposit: " + name);
        }

        if (elevationModifiers == null)
            elevationModifiers = new HashMap<>();
        if (temperatureModifiers == null)
            temperatureModifiers = new HashMap<>();
        if (humidityModifiers == null)
            humidityModifiers = new HashMap<>();
        if (points == null) {
            points = new HashMap<>();
        }

        for (BiomeGroup group : points.keySet()) {
            if (group == null) {
                System.err.println("Missing a biome group when initializing deposit " + name);
                continue;
            }
            for (Biome biome : group.getBiomes()) {
                this.points.put(biome, Math.max(0.0,
                        points.getOrDefault(group, 0.0)
                        + elevationModifiers.getOrDefault(biome.getElevation(), 0.0)
                        + temperatureModifiers.getOrDefault(biome.getTemperature(), 0.0)
                        + humidityModifiers.getOrDefault(biome.getHumidity(), 0.0)));
            }
        }

        for (Biome biome : Biome.Companion.getBiomes()) {
            if (this.points.containsKey(biome)) continue;
            this.points.put(biome, Math.max(0.0,
                            elevationModifiers.getOrDefault(biome.getElevation(), 0.0)
                            + temperatureModifiers.getOrDefault(biome.getTemperature(), 0.0)
                            + humidityModifiers.getOrDefault(biome.getHumidity(), 0.0)));
        }
    }

    //TODO change the biome format to allow for the use of modifiers.
    public double pointsIn(Biome biome) {
        return Math.max(points.getOrDefault(biome, 0.0), 0);
    }

    public String getId() {
        return name;
    }

    public String toString() {
        return getId();
    }
}
