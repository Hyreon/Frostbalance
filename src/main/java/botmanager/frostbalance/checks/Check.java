package botmanager.frostbalance.checks;

public interface Check {

    boolean validate();

    default String displayProblem() {
        return displayCondition();
    }

    String displayCondition();

}
