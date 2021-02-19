package botmanager.frostbalance.grid.biome;

public enum ElevationClass {

    NONE(Double.POSITIVE_INFINITY),

    MOUNTAINS(0.75),

    HILLS(0.55),
    PLAINS(0.35),

    BASIN(0.0); //being above sea level is no guarantee it's dry!

    double threshold;

    /**
     * Anything with an elevation higher than or at the threshold counts as this elevation class.
     * The highest elevation class is used.
     * @param threshold
     */
    ElevationClass(double threshold) {
        this.threshold = threshold;
    }

    public static ElevationClass from(double value) {
        double threshold = Double.NEGATIVE_INFINITY;
        ElevationClass selectedClass = null;
        for (ElevationClass elev : ElevationClass.values()) {
            if (elev.threshold > threshold && elev.threshold < value) {
                selectedClass = elev;
                threshold = elev.threshold;
            }
        }
        return selectedClass;
    }

}
