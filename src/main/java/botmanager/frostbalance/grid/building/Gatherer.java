package botmanager.frostbalance.grid.building;

import botmanager.Utilities;
import botmanager.frostbalance.Player;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.Tile;
import botmanager.frostbalance.resource.ResourceDeposit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class Gatherer extends Building {

    public final static int TO_LEVEL_UP = 360; //amounts to 6 hours, or 1/4 of a day

    double experience = 0.0;

    /**
     * FIXME BAD REFERENCE!
     * This reference would be serialized as a separate thing than the ResourceDeposit it is built on.
     */
    ResourceDeposit deposit;

    public Gatherer(Tile tile, Player owner, ResourceDeposit deposit) {
        super(tile, owner);
        this.deposit = deposit;
    }

    @Override
    public void setParent(Tile tile) {
        super.setParent(tile);

        //set the deposit to the appropriate resource, or invalidate this gatherer if no resource can be found
        for (ResourceDeposit deposit : tile.getResourceData().priorityOrderDeposits()) {
            if (deposit.equals(this.deposit)) {
                this.deposit = deposit;
                return;
            }
        }

        //no resource found that matches this one
        System.err.println("Failed to load gatherer " + this + " because its resource is missing! Sending it to the twilight zone.");
        setDisabled(true);

     }

    @Override
    public InputStream getRender() {
        try {
            return new FileInputStream(Utilities.getResource("textures/gatherer.png"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ResourceDeposit getDeposit() {
        return deposit;
    }

    public boolean turnAction() {

        System.out.println("Doing turn action!");

        List<PlayerCharacter> workers = WorkManager.singleton.getWorkers(this);
        double quantity = workers.isEmpty() ? 0 : (1.0 + getLevel()) / workers.size();
        for (PlayerCharacter worker : workers) {
            worker.getInventory().addItem(deposit.yield(quantity));
            gainExperience(); //there are 360 turns in a day
        }
        return false; //never any graphical change
    }

    private void gainExperience() {

        this.experience += 1.0 / TO_LEVEL_UP;

    }

    private double getLevel() {
        return Math.floor(Utilities.triangulateWithRemainder(experience));
    }

    public static final double RATE_NEVER = 0.0; //never
    public static final double RATE_MONTHLY = 1.0 / 2592000; //1 month
    public static final double RATE_WEEKLY = 1.0 / 604800; //1 week
    public static final double RATE_DAILY = 1.0 / 86400; //1 day
    public static final double RATE_INSTANT = 1.0; //1 minute

    public enum Method {

        //wells never run dry.
        WELL(RATE_NEVER, RATE_INSTANT, RATE_INSTANT),

        //farmed goods can be refreshed manually or automatically.
        PASTURE(RATE_WEEKLY, RATE_WEEKLY, RATE_MONTHLY / 2),
        PLANTATION(RATE_WEEKLY, RATE_MONTHLY, RATE_WEEKLY / 2),
        MILL(RATE_MONTHLY, RATE_MONTHLY, RATE_MONTHLY / 2),

        //wild goods are only refreshed automatically.
        BOATS(RATE_WEEKLY, RATE_NEVER, RATE_DAILY / 2),
        CAMP(RATE_DAILY, RATE_NEVER, RATE_WEEKLY / 2),

        //mineral goods are never refreshed.
        QUARRY(RATE_MONTHLY, RATE_NEVER, RATE_NEVER),
        MINE(RATE_MONTHLY, RATE_NEVER, RATE_NEVER),
        PIT(RATE_MONTHLY, RATE_NEVER, RATE_NEVER);

        double drainRate;
        double replantRate;
        double restoreRate;

        Method(double drainRate, double replantRate, double restoreRate) {
            this.drainRate = drainRate;
            this.replantRate = replantRate;
            this.restoreRate = restoreRate;
        }

        public double getDrainRate() {
            return drainRate;
        }

        public double getReplantRate() {
            return replantRate;
        }

        public double getRestoreRate() {
            return restoreRate;
        }
    }
}
