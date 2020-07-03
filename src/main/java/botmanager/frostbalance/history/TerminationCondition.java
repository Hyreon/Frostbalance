package botmanager.frostbalance.history;

public enum TerminationCondition {

    OTHER("Unknown"),
    NONE("Ongoing"),
    TRANSFER("Retired"),
    COUP("Deposed"),
    LEFT("Vanished");

    String name;

    TerminationCondition(String name) {
        this.name = name;
    }

}
