package botmanager.frostbalance.resource;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;

public class ResourceDeposit {

    String resourceId;
    int level;

    /**
     * Generates a resource deposit with a level up to the given amount.
     * @param resource
     * @param progress
     */
    public ResourceDeposit(Resource resource, int progress) {
        this.resourceId = resource.getId();
        this.level =
                (int) Utilities.mapToRange(Utilities.randomFromSeed(RandomId.DEPOSIT_LEVEL),
                        1, progress);
    }

    public String toString() {
        return getResource().name + " " + level;
    }

    public Resource getResource() {
        return Frostbalance.bot.resourceWithId(resourceId);
    }
}
