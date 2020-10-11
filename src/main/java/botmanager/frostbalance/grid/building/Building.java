package botmanager.frostbalance.grid.building;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Player;
import botmanager.frostbalance.grid.Tile;
import botmanager.frostbalance.grid.TileObject;
import org.jetbrains.annotations.NotNull;


//FIXME replace the existing building's ownerId to be not null. or just delete it
public abstract class Building extends TileObject {

    @NotNull
    public final String ownerId;

    protected Building(Tile tile, Player owner) {
        super(tile);
        ownerId = owner.getUserWrapper().getId();
    }

    public Player getOwner() {
        System.out.println("ownerId: " + ownerId);
        System.out.println("tile.getMap(): " + tile.getMap());
        return Frostbalance.bot.getUserWrapper(ownerId).playerIn(tile.getMap().getGameNetwork());
    }

    //It's a building. They don't do much.
    public boolean turnAction() {
        return false;
    }

}
