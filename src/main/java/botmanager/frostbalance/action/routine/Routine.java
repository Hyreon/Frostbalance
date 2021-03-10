package botmanager.frostbalance.action.routine;

import botmanager.frostbalance.action.actions.Action;
import botmanager.frostbalance.action.ActionQueue;
import botmanager.frostbalance.action.QueueStep;
import botmanager.frostbalance.checks.FrostbalanceException;

import java.util.Queue;

public abstract class Routine implements QueueStep {

    transient ActionQueue queue;

    /**
     * Gets the next action to take. Used when performing moves.
     * This returns 'null' if the action cannot be routine is complete, and the character should move on.
     * @return
     */
    public Action pollAction() {
        return peekAtAllActions().poll();
    }

    public Action peekAction() {
        return peekAtAllActions().peek();
    }

    public abstract Queue<? extends Action> peekAtAllActions();

    @Override
    public int moveCost() {
        return peekAction().moveCost();
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
