package botmanager.frostbalance.resource;

import botmanager.frostbalance.grid.biome.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class Resource {

    public String name;

    HashMap<Biome, Integer> points = new HashMap<>(); //odds of finding a specific resource

    Gatherer gatherer;

    public Resource(@NotNull String name) {
        this.name = name;
        for (Biome biome : Biome.values()) {
            points.put(biome, 9);
        }
        gatherer = Gatherer.PLANTATION;
    }

    public int pointsIn(Biome biome) {
        return points.getOrDefault(biome, 0);
    }

    public String getId() {
        return name;
    }

    public enum Gatherer {

        PASTURE,
        MINE,
        WELL,
        QUARRY,
        MILL,
        BOATS,
        PLANTATION,
        CAMP,
        PIT;

    }
}
