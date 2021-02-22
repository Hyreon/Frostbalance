package botmanager.frostbalance.resource;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.grid.Containable;
import botmanager.frostbalance.grid.coordinate.Hex;

public class ResourceDeposit implements Containable<ResourceData> {

    private transient ResourceData data;

    String resourceId;
    int maxSupplyFactor;
    double supply = 1.0; //1 for 100% supply, 0 for 0%

    /**
     * Generates a resource deposit with a level up to the given amount. Reseeded based on progress and tile location.
     * @param depositType
     * @param progress
     */
    public ResourceDeposit(DepositType depositType, Hex location, int progress) {
        this.resourceId = depositType.getId();
        this.maxSupplyFactor = 1;
        while ( Utilities.randomFromSeed(RandomId.DEPOSIT_LEVEL, location.getXnoZ(), location.getYnoZ(), progress, maxSupplyFactor) > 0.5 ) {
            maxSupplyFactor++;
        }
    }

    public String toString() {
        return maxSupplyFactor + "x " + getDeposit().name;
    }

    public DepositType getDeposit() {
        return Frostbalance.bot.resourceWithId(resourceId);
    }

    public ItemStack yield(double quantity) {
        return new ItemStack(getDeposit().itemType, quantity);
    }

    @Override
    public void setParent(ResourceData parent) {
        this.data = parent;
    }
}
