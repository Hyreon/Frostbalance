package botmanager.frostbalance.resource;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.grid.Containable;
import botmanager.frostbalance.grid.Tile;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ResourceData implements Containable<Tile> {

    transient Tile tile;

    /**
     * A list, in order, of the resources that have been discovered.
     */
    List<ResourceDeposit> resources = new ArrayList<>();

    /**
     * The total number of progress attempts.
     * This is used to seed success or failure, meaning the result is fixed
     * every time for a world, but hidden from the player.
     */
    private long attempts = 0;

    /**
     * The progress towards finding the next resource.
     * If this is full, a resource is added and progress is reset to 0.
     */
    private int progress = 0;

    public ResourceData(Tile tile) {
        this.tile = tile;
    }

    public int numResources() {
        return resources.size();
    }

    public boolean search() {
        //FIXME use the tile and the attempts to seed the result!
        if (Utilities.randomFromSeed(tile.getMap().getSeed(), tile.getLocation().hashCode(), attempts, RandomId.SEARCH_SUCCESS_FAIL) < 0.3) {
            addProgress();
            attempts++;
            return true;
        } else {
            resetProgress();
            attempts++;
            return false;
        }
    }

    private boolean addProgress() {
        progress++;
        if (progress > numResources()) {
            addResource();
            progress = 0;
            return true;
        }
        return false;
    }

    private void addResource() {
        long seed = Utilities.combineSeed(tile.getMap().getSeed(), tile.getLocation().hashCode(), attempts, RandomId.NEW_DEPOSIT);
        resources.add(
                new ResourceDeposit(Frostbalance.bot.generateResourceIn(tile.getBiome(), seed), progress)
        );
    }

    private void resetProgress() {
        progress = 0;
    }

    @Override
    public void setParent(Tile parent) {
        this.tile = parent;
    }

    @NotNull
    public List<ResourceDeposit> priorityOrderDeposits() {
        List<ResourceDeposit> sorted = new ArrayList<>(resources);
        sorted.sort(Comparator.comparingInt(i -> -i.level));
        return sorted;
    }

    /**
     * Simplifies a list of resource deposits. This modifies the original list.
     * Note that this list assumes older values are first, and gives them precedence,
     * removing any newer (later) values if they have the same level and resource type as another.
     * This also assumes the initial order is by priority order, listing highest values first.
     * @param resourceDepositList
     */
    public static void simp(List<ResourceDeposit> resourceDepositList) {
        HashMap<ResourceDepositType, List<ResourceDeposit>> resourceDepositBuckets = new HashMap<>();
        for (int i = 0; i < resourceDepositList.size(); i++) {
            ResourceDeposit deposit = resourceDepositList.get(i);
            if (resourceDepositBuckets.containsKey(deposit.getResource())) {
                resourceDepositBuckets.get(deposit.getResource()).add(deposit);
            } else {
                List<ResourceDeposit> initial = new ArrayList<>();
                initial.add(deposit);
                resourceDepositBuckets.put(deposit.getResource(), initial);
            }
        }
        System.out.println("Buckets assembled: " + resourceDepositBuckets.toString());
        for (ResourceDepositType key : resourceDepositBuckets.keySet()) {
            int max = -1;
            for (ResourceDeposit resourceDeposit : resourceDepositBuckets.get(key)) {
                if (resourceDeposit.level <= max) {
                    resourceDepositList.remove(resourceDeposit);
                    System.out.println("Removed: " + resourceDepositBuckets.toString());
                } else {
                    max = resourceDeposit.level;
                }
            }
        }
    }

    public int getProgress() {
        return progress;
    }
}
