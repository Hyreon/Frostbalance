package botmanager.frostbalance.action.routine;

import botmanager.frostbalance.action.Action;
import botmanager.frostbalance.action.QueueStep;
import botmanager.frostbalance.checks.FrostbalanceException;
import botmanager.frostbalance.grid.PlayerCharacter;

import java.util.Queue;

public abstract class Routine implements QueueStep {

    PlayerCharacter mobile;

    public Action pollAction() {
        return peekAtAllActions().poll();
    }

    public abstract Queue<? extends Action> peekAtAllActions();

    @Override
    public int moveCost() {
        return pollAction().moveCost();
    }

    @Override
    public void doAction() throws FrostbalanceException {
        pollAction().doAction();
    }

}
