package botmanager.frostbalance.action.routine;

import botmanager.frostbalance.action.QueueStep;
import botmanager.frostbalance.action.actions.Action;
import botmanager.frostbalance.action.actions.SearchAction;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.resource.ResourceData;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class SearchRoutine extends Routine {

    int targetItemAmount;

    public SearchRoutine(PlayerCharacter character, int targetItemAmount) {
        setParent(character.getActionQueue());
        this.targetItemAmount = targetItemAmount;
    }

    @Override
    public Queue<? extends Action> peekAtAllActions() {
        Queue<SearchAction> searches = new LinkedBlockingQueue<>();
        ResourceData tileResourceData = queue.getCharacter().getTile().getResourceData();
        int estimatedSearches = (tileResourceData.numResources() + 1) - tileResourceData.getProgress();
        for (int i = 0; i < estimatedSearches; i++) {
            searches.add(new SearchAction(queue.getCharacter()));
        }
        return searches;
    }

    @Override
    public SearchAction pollAction() {
        return peekAction();
    }

    @Override
    public SearchAction peekAction() {
        if (queue.getCharacter().getTile().getResourceData().numResources() >= targetItemAmount) {
            return null;
        }
        else {
            return new SearchAction(queue.getCharacter());
        }
    }

    @Override
    public QueueStep simulate() {
        return new SearchRoutine(queue.getCharacter(), targetItemAmount);
    }

    @Override
    public String displayStep() {
        return "**Search this tile until " + targetItemAmount + " item(s) are available**";
    }

}
