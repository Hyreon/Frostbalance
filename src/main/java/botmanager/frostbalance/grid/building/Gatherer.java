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

    public final static int TO_LEVEL_UP = 360;

    double experience = 0.0;

    ResourceDeposit deposit;

    public Gatherer(Tile tile, Player owner, ResourceDeposit deposit) {
        super(tile, owner);
        this.deposit = deposit;
    }

    @Override
    public InputStream getRender() {
        try {
            return new FileInputStream("res/gatherer.png");
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
            gainExperience( 1.0 / 360 ); //there are 360 turns in a day
        }
        return false; //never any graphical change
    }

    private void gainExperience(double experience) {

        this.experience += experience;

    }

    private double getLevel() {
        return Utilities.triangulateWithRemainder(experience);
    }

    public enum Method {

        PASTURE,
        MINE,
        WELL,
        QUARRY,
        MILL,
        BOATS,
        PLANTATION,
        CAMP,
        PIT

    }
}
