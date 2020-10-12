package botmanager.frostbalance.checks;

import botmanager.frostbalance.grid.Tile;

public class PassableTileValidator implements Check {

    Tile tile;

    public PassableTileValidator(Tile tile) {
        this.tile = tile;
    }

    @Override
    public boolean validate() {
        return true; //no impassable tiles (yet)
    }

    @Override
    public String displayCondition() {
        return "The target tile " + tile.getLocation().toString() + " must be passable.";
    }
}
