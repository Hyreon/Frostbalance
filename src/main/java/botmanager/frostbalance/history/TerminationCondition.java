package botmanager.frostbalance.history;

public enum TerminationCondition {

    UNKNOWN("Unknown"),
    NONE("Ongoing"),
    TRANSFER("Retired"),
    COUP("Deposed"),
    LEFT("Vanished"),
    RESET("The Plot Demanded It");

    String name;

    TerminationCondition(String name) {
        this.name = name;
    }

}
