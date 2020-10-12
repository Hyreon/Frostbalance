package botmanager.frostbalance.checks;

import botmanager.frostbalance.Influence;
import botmanager.frostbalance.MemberWrapper;
import botmanager.frostbalance.grid.Claim;

public class InfluenceMinimumValidator implements Check {

    Influence actual;
    Influence minimum;

    public InfluenceMinimumValidator(MemberWrapper memberWrapper, Influence minimum) {
        this.actual = memberWrapper.getInfluence();
        this.minimum = minimum;
    }

    public InfluenceMinimumValidator(Claim claim, Influence minimum) {
        this.actual = claim.getInvestedStrength();
        this.minimum = minimum;
    }

    @Override
    public boolean validate() {
        return actual.compareTo(minimum) >= 0;
    }

    @Override
    public String displayCondition() {
        return "You must have enough influence.";
    }
}
