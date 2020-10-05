package botmanager.frostbalance.resource;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;

public class ResourceDeposit {

    String resourceId;
    int level;

    /**
     * Generates a resource deposit with a level up to the given amount.
     * @param mapResource
     * @param progress
     */
    public ResourceDeposit(MapResource mapResource, int progress) {
        this.resourceId = mapResource.getId();
        this.level =
                (int) Utilities.mapToRange(Utilities.randomFromSeed(RandomId.DEPOSIT_LEVEL),
                        1, progress);
    }

    public String toString() {
        return getDeposit().name + " " + level;
    }

    public MapResource getDeposit() {
        return Frostbalance.bot.resourceWithId(resourceId);
    }

    public ItemStack yield(double quantity) {
        return new ItemStack(getDeposit().itemType, quantity, level);
    }
}
