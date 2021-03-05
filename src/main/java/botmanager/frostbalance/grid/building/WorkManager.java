package botmanager.frostbalance.grid.building;

import botmanager.frostbalance.grid.PlayerCharacter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A static singleton class that manages the relationship between PlayerCharacters and Buildings.
 * Exactly one exists per bot.
 * Expect to see more of these. 10/2/2020
 */
public class WorkManager {

    public static WorkManager singleton = new WorkManager();

    private WorkManager() {}

    List<PlayerCharacter> allWorkers = new ArrayList<>();

    /**
     *
     * @param worker The worker working
     * @return False, if this worker was not added (because they were already working)
     */
    public boolean addWorker(PlayerCharacter worker) {
        if (allWorkers.contains(worker)) {
            return false;
        }
        return allWorkers.add(worker);
    }

    public void completeWorkCycle() {
        allWorkers.clear();
    }

    public List<PlayerCharacter> getWorkers(Building building) {
        List<PlayerCharacter> localWorkers = new LinkedList<>();
        for (PlayerCharacter worker : allWorkers) {
            if (worker.getLocation().equals(building.getLocation())) {
                localWorkers.add(worker);
            }
        }
        return localWorkers;
    }

    /**
     * Corrects any workers or buildings in an invalid state.
     */
    /*
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
                } else if (workingSomewhere.contains(worker)) {
                    System.err.println("Removing worker multi-task");
                    removeWorker(key, worker);
                } else if (!(worker.getActionQueue().peek() instanceof WorkAction)) {
                    removeWorker(key, worker);
                } else { //the worker works
                    workingSomewhere.add(worker);
                }
            }
        }

        if (!allWorkers.containsAll(workingSomewhere) || allWorkers.size() != workingSomewhere.size()) {
            System.err.println("Correcting worker-count desynchronization");
            allWorkers = workingSomewhere;
        }

    }*/

}
