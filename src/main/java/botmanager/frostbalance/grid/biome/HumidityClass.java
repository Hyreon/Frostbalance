package botmanager.frostbalance.grid.biome;

public enum HumidityClass {

    NONE(Double.POSITIVE_INFINITY),

    HEAVY_RAIN(23.0 / 32),

    GOOD_RAIN(0.5),
    MODEST_RAIN(9.0 / 32),

    ARID(0.0);

    double threshold;

    /**
     * Anything with a humidity higher than or at the threshold counts as this humidity class.
     * The highest humidity class is used.
     * @param threshold
     */
    HumidityClass(double threshold) {
        this.threshold = threshold;
    }

    public static HumidityClass from(double value) {
        double threshold = Double.NEGATIVE_INFINITY;
        HumidityClass selectedClass = null;
        for (HumidityClass watr : HumidityClass.values()) {
            if (watr.threshold > threshold && watr.threshold < value) {
                selectedClass = watr;
                threshold = watr.threshold;
            }
        }
        return selectedClass;
    }

}
