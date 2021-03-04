package botmanager.frostbalance.resource;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.grid.Containable;
import botmanager.frostbalance.grid.Tile;
import botmanager.frostbalance.grid.building.Building;
import botmanager.frostbalance.grid.building.Gatherer;

public class ResourceDeposit implements Containable<ResourceData> {

    private transient ResourceData data;

    String resourceId;
    int maxSupplyFactor;
    long lastSupplyUpdateTurn = 0;
    double supply = 1.0; //1 for 100% supply, 0 for 0%

    /**
     * Generates a resource deposit with a level up to the given amount. Reseeded based on progress and tile location.
     * @param depositType
     * @param progress
     */
    public ResourceDeposit(DepositType depositType, Tile tile, int progress) {
        this.data = tile.getResourceData();
        this.resourceId = depositType.getId();
        this.maxSupplyFactor = 1;
        while ( Utilities.randomFromSeed(RandomId.DEPOSIT_LEVEL, tile.getLocation().getXnoZ(), tile.getLocation().getYnoZ(), progress, maxSupplyFactor) > 0.5 ) {
            maxSupplyFactor++;
        }
    }

    public String toString() {
        return maxSupplyFactor + "x " + getDeposit().name;
    }

    public DepositType getDeposit() {
        return Frostbalance.bot.resourceWithId(resourceId);
    }

    /**
     * Attempts to yield an item. If no item can be yielded, then null is returned instead.
     * Supply increases or decreases when this command is run.
     * @param quantity
     * @return
     */
    public ItemStack yield(double quantity) {
        if (getSupply() > 0) {
            drainSupply(quantity);
            return new ItemStack(getDeposit().itemType, quantity);
        } else {
            replenishSupply(quantity);
            return null;
        }
    }

    private void drainSupply(double quantity) {
        getSupply();
        supply -= getDeposit().gatherMethod.getDrainRate() * quantity / maxSupplyFactor;
    }

    private void replenishSupply(double quantity) {
        getSupply();
        supply += getDeposit().gatherMethod.getReplantRate() * quantity / maxSupplyFactor;
        if (supply > 1.0) { supply = 1.0; }
    }

    private double getSupply() {
        long currentTurn = data.tile.getMap().getGameNetwork().getTurn();
        if (lastSupplyUpdateTurn < currentTurn) {
            renewSupply(currentTurn - lastSupplyUpdateTurn);
            lastSupplyUpdateTurn = currentTurn;
        }
        return supply;
    }

    /**
     * Returns the building placed on this resource, or null if there is no such building.
     */
    private Gatherer getBuilding() {
        for (Building building : data.tile.getBuildingData().getBuildings()) {
            if (building instanceof Gatherer) {
                if (((Gatherer) building).getDeposit() == this) {
                    return (Gatherer) building;
                }
            }
        }
        return null;
    }

    private void renewSupply(long turns) {
        supply += getDeposit().gatherMethod.getRestoreRate() * turns / maxSupplyFactor;
        if (supply > 1.0) { supply = 1.0; }
    }

    @Override
    public void setParent(ResourceData parent) {
        this.data = parent;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ResourceDeposit) &&
                ((ResourceDeposit) o).data == data &&
                ((ResourceDeposit) o).resourceId.equals(resourceId) &&
                ((ResourceDeposit) o).maxSupplyFactor == maxSupplyFactor;
    }

}
