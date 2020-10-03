package botmanager.frostbalance.resource;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;

public class ResourceDeposit {

    String resourceId;
    int level;

    /**
     * Generates a resource deposit with a level up to the given amount.
     * @param resourceDepositType
     * @param progress
     */
    public ResourceDeposit(ResourceDepositType resourceDepositType, int progress) {
        this.resourceId = resourceDepositType.getId();
        this.level =
                (int) Utilities.mapToRange(Utilities.randomFromSeed(RandomId.DEPOSIT_LEVEL),
                        1, progress);
    }

    public String toString() {
        return getResource().name + " " + level;
    }

    public ResourceDepositType getResource() {
        return Frostbalance.bot.resourceWithId(resourceId);
    }
}
