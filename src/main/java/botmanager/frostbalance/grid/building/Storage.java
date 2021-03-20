package botmanager.frostbalance.grid.building;

import botmanager.Utilities;
import botmanager.frostbalance.Player;
import botmanager.frostbalance.grid.Tile;
import botmanager.frostbalance.resource.Inventory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class Storage extends Building {

    /**
     * Different inventories can have different permissions, hence the plurality.
     */
    List<Inventory> inventories;

    protected Storage(Tile tile, Player owner) {
        super(tile, owner);
    }

    @Override
    public InputStream getRender() {
        try {
            return new FileInputStream(Utilities.getResource("textures/worksite.png"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
