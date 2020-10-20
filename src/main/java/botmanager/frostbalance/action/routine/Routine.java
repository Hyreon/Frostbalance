package botmanager.frostbalance.action.routine;

import botmanager.frostbalance.action.actions.Action;
import botmanager.frostbalance.action.ActionQueue;
import botmanager.frostbalance.action.QueueStep;
import botmanager.frostbalance.checks.FrostbalanceException;

import java.util.Queue;

public abstract class Routine implements QueueStep {

    transient ActionQueue queue;

    public Action pollAction() {
        return peekAtAllActions().poll();
    }

    public Action peekAction() {
        return peekAtAllActions().peek();
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

    @Override
    public void setParent(ActionQueue queue) {
        this.queue = queue;
    }

}
