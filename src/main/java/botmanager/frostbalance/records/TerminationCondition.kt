package botmanager.frostbalance.records

enum class TerminationCondition(var displayText: String) {
    UNKNOWN("Unknown"), NONE("Ongoing"), TRANSFER("Retired"), COUP("Deposed"), LEFT("Vanished"), RESET("Admin Action");
}