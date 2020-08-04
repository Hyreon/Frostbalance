package botmanager.frostbalance;

import java.time.LocalDate;

public class DailyInfluenceSource {

    public static final Influence DAILY_INFLUENCE_CAP = new Influence(1.00);

    Influence influenceLeft;
    long dailyDate;

    public DailyInfluenceSource() {
        this.influenceLeft = DAILY_INFLUENCE_CAP;
        dailyDate = LocalDate.now().toEpochDay();
    }

    public DailyInfluenceSource(Influence influenceTaken, long dailyDate) {
        this.influenceLeft = DAILY_INFLUENCE_CAP.subtract(influenceTaken);
        this.dailyDate = dailyDate;
    }

}
