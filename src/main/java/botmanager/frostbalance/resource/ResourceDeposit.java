package botmanager.frostbalance.resource;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.grid.Containable;
import botmanager.frostbalance.grid.coordinate.Hex;

public class ResourceDeposit implements Containable<ResourceData> {

    private transient ResourceData data;

    String resourceId;
    int level;

    /**
     * Generates a resource deposit with a level up to the given amount. Reseeded based on progress and tile location.
     * @param mapResource
     * @param progress
     */
    public ResourceDeposit(MapResource mapResource, Hex location, int progress) {
        this.resourceId = mapResource.getId();
        this.level =
                (int) Utilities.mapToRange(Utilities.randomFromSeed(RandomId.DEPOSIT_LEVEL, location.getXnoZ(), location.getYnoZ(), progress),
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

    @Override
    public void setParent(ResourceData parent) {
        this.data = parent;
    }
}
