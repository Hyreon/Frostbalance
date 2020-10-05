package botmanager.frostbalance.grid.building;

import botmanager.frostbalance.MapToCollection;
import botmanager.frostbalance.grid.PlayerCharacter;

import java.util.*;

/**
 * A static singleton class that manages the relationship between PlayerCharacters and Buildings.
 * Exactly one exists per bot.
 * Expect to see more of these. 10/2/2020
 */
public class WorkManager {

    public static WorkManager singleton = new WorkManager();

    private WorkManager() {
        Timer validatorTimer = new Timer();
        validatorTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                validateWorkers();
            }
        }, 300000, 300000);
    }

    Map<Building, List<PlayerCharacter>> workers = new MapToCollection<>();
    Map<PlayerCharacter, Building> buildings = new HashMap<>();
    List<PlayerCharacter> allWorkers = new ArrayList<>();

    public void addWorker(Building building, PlayerCharacter worker) {
        workers.getOrDefault(building, new ArrayList<>()).add(worker);
        buildings.put(worker, building);
        allWorkers.add(worker);
    }

    public boolean removeWorker(Building building, PlayerCharacter worker) {
        allWorkers.remove(worker);
        buildings.remove(worker);
        return workers.getOrDefault(building, new ArrayList<>()).remove(worker);
    }

    public Building getBuilding(PlayerCharacter worker) {
        for (Building building : workers.keySet()) {
            if (workers.get(building).contains(worker)) {
                return building;
            }
        }
        return null;
    }

    public boolean isWorking(PlayerCharacter worker) {
        return allWorkers.contains(worker);
    }

    public List<PlayerCharacter> getWorkers(Building building) {
        return workers.get(building);
    }

    public void shutdown(Building building) {
        for (PlayerCharacter worker : workers.get(building)) {
            removeWorker(building, worker);
        }
    }

    /**
     * Corrects any workers or buildings in an invalid state.
     */
    public void validateWorkers() {
        List<PlayerCharacter> workingSomewhere = new ArrayList<>();
        for (Building key : workers.keySet()) {
            List<PlayerCharacter> characters = workers.get(key);
            Iterator<PlayerCharacter> characterIter = characters.iterator();
            while (characterIter.hasNext()) {
                PlayerCharacter worker = characterIter.next();
                if (!worker.getTile().equals(key.getTile())) { //invalid!
                    System.err.println("Removing worker-building desynchronization");
                    removeWorker(key, worker);
                }
                if (workingSomewhere.contains(worker)) {
                    System.err.println("Removing worker multi-task");
                    removeWorker(key, worker);
                }
                workingSomewhere.add(worker);
            }
        }

        if (!allWorkers.containsAll(workingSomewhere) || allWorkers.size() != workingSomewhere.size()) {
            System.err.println("Correcting worker-count desynchronization");
            allWorkers = workingSomewhere;
        }

    }

}
