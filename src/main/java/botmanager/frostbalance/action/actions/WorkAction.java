package botmanager.frostbalance.action.actions;

import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.building.WorkManager;

public class WorkAction extends Action {

    public WorkAction(PlayerCharacter character) {
        super(character);
    }

    @Override
    public int moveCost() {
        return (validWorkConditions()) ? 1 : 0;
    }

    private boolean validWorkConditions() {
        return queue.getCharacter().getTile().getBuildingData().allowsWork(queue.getCharacter());
    }

    @Override
    public void doAction() {

        WorkManager.singleton.addWorker(queue.getCharacter());

    }

    @Override
    public String displayStep() {
        return "Work at this tile's building";
    }

}