package botmanager.frostbalance.checks;

import java.util.ArrayList;
import java.util.List;

public class ValidationTests {

    public ValidationTests() {
    }

    public void throwIfAny(Check... checks) throws FrostbalanceException {

        List<Check> failedChecks = new ArrayList<>();

        for (Check check : checks) {
            if (!check.validate()) failedChecks.add(check);
        }

        if (!failedChecks.isEmpty()) {
            throw new FrostbalanceException(failedChecks.toArray(new Check[]{}));
        }
    }

}
