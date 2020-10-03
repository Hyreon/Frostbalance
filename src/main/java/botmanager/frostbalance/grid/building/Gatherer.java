package botmanager.frostbalance.grid.building;

import botmanager.frostbalance.grid.Tile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Gatherer extends Building {

    protected Gatherer(Tile tile) {
        super(tile);
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
