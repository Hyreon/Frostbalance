package botmanager.frostbalance.action.actions;

import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.building.Gatherer;
import botmanager.frostbalance.grid.building.WorkManager;

public class WorkAction extends Action {

    public WorkAction(PlayerCharacter character) {
        super(character);
    }

    @Override
    public int moveCost() {
        return (queue.getCharacter().getTile().getBuildingData().allowsWork(queue.getCharacter())) ? 1 : 0;
    }

    @Override
    public void doAction() {

        Gatherer gatherer = queue.getCharacter().getTile().getBuildingData().activeGatherer();

        if (gatherer != null) {
            WorkManager.singleton.addWorker(queue.getCharacter());
        } else {
            //TODO cancel and refund movement cost
        }

    }

    @Override
    public String displayStep() {
        return "Work at this tile's building";
    }

}