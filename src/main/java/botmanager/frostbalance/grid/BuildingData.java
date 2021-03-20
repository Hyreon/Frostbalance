package botmanager.frostbalance.grid;

import botmanager.frostbalance.Player;
import botmanager.frostbalance.grid.building.*;
import botmanager.frostbalance.resource.ResourceDeposit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BuildingData implements Containable<Tile>, Container {

    transient Tile tile;

    /**
     * A reference to all past and current gatherers on this location.
     * Now uses activeBuilding and ruins.
     */
    @Deprecated
    List<Gatherer> gatherers = new ArrayList<>();

    /**
     * The active building on this tile. There can only be one or none.
     */
    Building activeBuilding = null;

    /**
     * A reference to buildings no longer being used.
     * They can be repaired by the original owner, if they own the land, and behave as if they were never
     * dismantled.
     * Buildings are automatically dismantled if a new claim is made, preventing the owner from accessing their
     * building.
     */
    List<Building> ruins = new ArrayList<>();


    /**
     * Gets all buildings, active or otherwise.
     * @return A newly created list containing all buildings
     */
    @NotNull
    public List<Building> getBuildings() {
        if (ruins == null) ruins = new ArrayList<>();
        List<Building> allBuildings = new ArrayList<>(ruins);
        if (getActiveBuilding() != null) allBuildings.add(getActiveBuilding());
        return allBuildings;
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
        if (gatherers != null) {
            ruins = new ArrayList<>();
            for (Gatherer gatherer : gatherers) {
                ruins.add(gatherer);
                gatherer.setParent(this.tile);
            }
            gatherers = null;
        }
    }

    @Nullable
    public Gatherer getActiveGatherer() {
        Building activeBuilding = getActiveBuilding();
        if (activeBuilding instanceof Gatherer) return (Gatherer) activeBuilding;
        else return null;
    }

    public Building getActiveBuilding() {
        return activeBuilding;
    }

    public boolean allowsWork(PlayerCharacter character) {
        return (getActiveGatherer() != null && getActiveGatherer().ownerId.equals(character.userId));
    }

    @Nullable
    public Gatherer gathererOf(@NotNull ResourceDeposit resource) {
        for (Building building : getBuildings()) {
            if (building instanceof Gatherer) {
                if (((Gatherer) building).getDeposit().equals(resource))
                    return (Gatherer) building;
            }
        }
        return null;
    }

    public Housing housingFor(@NotNull Player player) {
        for (Building building : getBuildings()) {
            if (building instanceof Housing) {
                if (building.getOwner().equals(player))
                    return (Housing) building;
            }
        }
        return null;
    }

    public Workshop worksiteOf(@NotNull WorkshopType option) {
        for (Building building : getBuildings()) {
            if (building instanceof Workshop) {
                if (((Workshop) building).getWorksiteType().equals(option))
                    return (Workshop) building;
            }
        }
        return null;
    }

    /**
     * Adds a building as the new default.
     * @param building
     */
    public void addBuilding(@NotNull Building building) {
        if (getActiveBuilding() != null) {
            ruins.add(activeBuilding);
        }
        activeBuilding = building;
    }

    public void activateBuilding(@NotNull Building building) {
        if (getBuildings().contains(building)) {
            if (getActiveBuilding() != null) {
                ruins.add(activeBuilding);
                activeBuilding = building;
                ruins.remove(building);
            }
        } else {
            //nothing lol
        }
    }
}
