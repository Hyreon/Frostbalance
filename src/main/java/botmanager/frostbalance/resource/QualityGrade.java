package botmanager.frostbalance.resource;

public enum QualityGrade {

    WORTHLESS("Worthless"),
    CRUDE("Crude"),
    STANDARD("Standard"),
    GOOD("Good"),
    VERY_GOOD("Very Good"),
    EXCELLENT("Excellent"),
    VERY_EXCELLENT("Very Excellent"),
    INCREDIBLE("Incredible"),
    EPIC("Epic"),
    LEGENDARY("Legendary"),
    MYTHIC("Mythic");

    String displayName;

    QualityGrade(String displayName) {
        this.displayName = displayName;
    }

    public static String asString(int quality) {
        if (quality <= 0) {
            int degrees = -quality;
            return QualityGrade.values()[0].displayName() + "-".repeat(degrees);
        } else if (quality >= QualityGrade.values().length) {
            int degrees = quality - QualityGrade.values().length + 1;
            return QualityGrade.values()[QualityGrade.values().length - 1].displayName() + degrees;
        } else {
            return QualityGrade.values()[quality].displayName();
        }
    }

    public String displayName() {
        return displayName;
    }
}
