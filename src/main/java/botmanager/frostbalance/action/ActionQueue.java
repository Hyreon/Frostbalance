package botmanager.frostbalance.action;

import botmanager.frostbalance.action.routine.Routine;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

public class ActionQueue extends ArrayBlockingQueue<QueueStep> {

    public ActionQueue() {
        super(11);
    }

    @Override
    public Action poll() {
        QueueStep nextStep = peek();
        if (nextStep instanceof Action) {
            return (Action) super.poll();
        } else if (nextStep instanceof Routine) {
            Action nextAction = ((Routine) nextStep).pollAction();
            if (nextAction == null) {
                super.poll();
                return poll();
            } else {
                return nextAction;
            }
        } else {
            throw new IllegalStateException("Unknown queue step! Only routines and actions are supported");
        }
    }

    public QueueStep pollBase() {
        return super.poll();
    }

    /**
     * Creates a clone of this ActionQueue that does not modify the existing game state (unless you want it to).
     * @return
     */
    public ActionQueue simulator() {
        ActionQueue simulation = new ActionQueue();
        Iterator<QueueStep> iterator = iterator();
        while (iterator.hasNext()) {
            QueueStep step = iterator.next();
            simulation.add(step.simulate());
        }
        simulation.addAll(this);
        return simulation;
    }
}
