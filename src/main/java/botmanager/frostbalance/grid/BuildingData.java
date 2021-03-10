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

    /**
     * Adds a new gatherer. This gatherer is *not* added to the history if a functionally identical one exists.
     * @param gatherer
     * @return Whether the new gatherer was added (true) or simply redirected to an existing one (false).
     */
    public boolean addGatherer(@NotNull Gatherer gatherer) {
        for (int i = 0; i < gatherers.size(); i++) {
            Gatherer existingGatherer = gatherers.get(i);
            if (existingGatherer.ownerId.equals(gatherer.ownerId)
                    && existingGatherer.getDeposit().equals(gatherer.getDeposit())) {
                gatherers.remove(existingGatherer);
                gatherers.add(0, existingGatherer);
                return false;
            }
        }
        gatherers.add(0,gatherer); //this gatherer gets precedence, as it is newer
        return true;
    }

    public boolean allowsWork(PlayerCharacter character) {
        return (activeGatherer() != null && activeGatherer().ownerId.equals(character.userId));
    }
}
