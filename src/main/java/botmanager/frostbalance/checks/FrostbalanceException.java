package botmanager.frostbalance.checks;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A unique sort of exception that can carry any number of problems.
 * These exceptions are NEVER meant to block more than a small amount of a program's execution.
 */
public class FrostbalanceException extends Exception {

    List<Check> causes;

    public FrostbalanceException(Check... causes) {
        this.causes = Arrays.asList(causes);
    }

    public List<String> displayCauses() {
        return causes.stream().map(Object::toString).collect(Collectors.toList());
    }
}
