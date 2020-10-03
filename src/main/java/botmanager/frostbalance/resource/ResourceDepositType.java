package botmanager.frostbalance.resource;

import botmanager.frostbalance.grid.building.Gatherer;
import botmanager.frostbalance.grid.biome.Biome;
import jdk.internal.loader.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ResourceDepositType {

    public String name;

    public Resource resource;

    HashMap<Biome, Integer> points = new HashMap<>(); //odds of finding a specific resource

    Gatherer.Method gatherer;

    public ResourceDepositType(@NotNull String name) {
        this.name = name;
        for (Biome biome : Biome.values()) {
            points.put(biome, 9);
        }
        gatherer = Gatherer.Method.PLANTATION;
    }

    public int pointsIn(Biome biome) {
        return points.getOrDefault(biome, 0);
    }

    public String getId() {
        return name;
    }
}
