package botmanager.frostbalance.grid.biome;

public enum TemperatureClass {

    NONE(Double.POSITIVE_INFINITY),

    TROPICAL(0.7),

    WARM(0.5),
    COOL(0.3),

    BOREAL(0.0);

    double threshold;

    /**
     * Anything with a temperature higher than or at the threshold counts as this temperature class.
     * The highest temperature class is used.
     * @param threshold
     */
    TemperatureClass(double threshold) {
        this.threshold = threshold;
    }

    public static TemperatureClass from(double value) {
        double threshold = Double.NEGATIVE_INFINITY;
        TemperatureClass selectedClass = null;
        for (TemperatureClass temp : TemperatureClass.values()) {
            if (temp.threshold > threshold && temp.threshold < value) {
                selectedClass = temp;
                threshold = temp.threshold;
            }
        }
        return selectedClass;
    }
}
