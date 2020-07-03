package botmanager.frostbalance.history;

import botmanager.Utilities;

public class RegimeData {

    String userId;
    long startTime;
    long endTime;
    TerminationCondition terminationCondition;

    public RegimeData(String userId, TerminationCondition terminationCondition, long startDay, long endDay) {
        this.userId = userId;
        this.terminationCondition = terminationCondition;
        this.startTime = startDay;
        this.endTime = endDay;
    }

    public String toCSV() {
        return String.join(",", userId, String.valueOf(startTime), String.valueOf(endTime), terminationCondition.name());
    }

    public void end(TerminationCondition condition) {
        endTime = Utilities.todayAsLong();
        terminationCondition = condition;
    }
}
