package botmanager.frostbalance.grid;

import botmanager.frostbalance.grid.building.Building;
import botmanager.frostbalance.grid.building.Gatherer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BuildingData implements Containable<Tile>, Container {

    transient Tile tile;

    /**
     * A reference to all past and current gatherers on this location.
     */
    List<Gatherer> gatherers = new ArrayList<>();

    @NotNull
    public List<Building> getBuildings() {
        return new ArrayList<>(gatherers);
    };

    public BuildingData(Tile tile) {
        this.tile = tile;
    }

    @Override
    public void setParent(Tile parent) {
        this.tile = parent;
    }

    @Override
    public void adopt() {
        for (Building building : getBuildings()) {
            building.setParent(this.tile); //shrug
        }
    }

    @Nullable
    public Gatherer activeGatherer() {
        if (gatherers.isEmpty()) return null;
        else return gatherers.get(0);
    }

    public void addGatherer(@NotNull Gatherer gatherer) {
        gatherers.add(gatherer);
    }
}
