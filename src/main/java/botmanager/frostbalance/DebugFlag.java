package botmanager.frostbalance;

public class DebugFlag {

    String label;

    public DebugFlag(String label) {
        this.label = label.toUpperCase();
    }

    public enum Default {
        MAIN("MAIN");

        DebugFlag debugFlag;

        Default(String label) {
            debugFlag = new DebugFlag(label);
        }
    }
}
