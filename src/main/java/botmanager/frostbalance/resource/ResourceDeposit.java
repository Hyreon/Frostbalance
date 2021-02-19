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
     * @param depositType
     * @param progress
     */
    public ResourceDeposit(DepositType depositType, Hex location, int progress) {
        this.resourceId = depositType.getId();
        this.level =
                (int) Utilities.mapToRange(Utilities.randomFromSeed(RandomId.DEPOSIT_LEVEL, location.getXnoZ(), location.getYnoZ(), progress),
                        1, progress);
    }

    public String toString() {
        return QualityGrade.asString(level) + " " + getDeposit().name;
    }

    public DepositType getDeposit() {
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
