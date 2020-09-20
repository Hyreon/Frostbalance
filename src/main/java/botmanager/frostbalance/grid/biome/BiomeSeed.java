package botmanager.frostbalance.grid.biome;

public enum BiomeSeed {

    HEAT(0),
    DEPTH(1),
    HUMIDITY(-1);

    final long seed;

    BiomeSeed(int seed) {
        this.seed = seed;
    }

}
