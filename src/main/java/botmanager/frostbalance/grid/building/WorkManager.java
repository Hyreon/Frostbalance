package botmanager.frostbalance.grid.building;

import botmanager.frostbalance.HotMap;
import botmanager.frostbalance.grid.PlayerCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkManager {

    Map<Building, List<PlayerCharacter>> workers = new HotMap<>();

    public void addWorker(Building building, PlayerCharacter worker) {
        workers.getOrDefault(building, new ArrayList<>()).add(worker);
    }

    public boolean removeWorker(Building building, PlayerCharacter worker) {
        return workers.getOrDefault(building, new ArrayList<>()).remove(worker);
    }

    public void validateWorkers() {
        for (Building key : workers.keySet()) {
            List<PlayerCharacter> characters = workers.get(key);
            for (int i = 0; i < characters.size(); i++) {
                characters.
            }
        }
    }

}
