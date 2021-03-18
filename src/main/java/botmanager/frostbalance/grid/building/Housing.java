package botmanager.frostbalance.grid.building;

import botmanager.Utilities;
import botmanager.frostbalance.Player;
import botmanager.frostbalance.grid.Tile;
import botmanager.frostbalance.resource.HousingInventory;
import botmanager.frostbalance.resource.Inventory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Housing extends Building {

    Inventory inventory = new HousingInventory();

    public Housing(Tile tile, Player owner) {
        super(tile, owner);
    }

    @Override
    public InputStream getRender() {
        try {
            return new FileInputStream(Utilities.getResource("textures/housing.png"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
