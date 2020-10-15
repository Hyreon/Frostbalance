package botmanager.frostbalance.action;

import botmanager.frostbalance.action.routine.MoveToRoutine;
import botmanager.frostbalance.action.routine.RepeatRoutine;
import botmanager.frostbalance.action.routine.Routine;
import botmanager.frostbalance.grid.coordinate.Hex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class ActionQueue extends LinkedBlockingQueue<QueueStep> {

    public ActionQueue() {
        super();
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
        } else if (nextStep == null) {
            return null;
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
        return simulation;
    }

    /**
     * Destroys the current simulation (without firing anything) to get a list of moves.
     * @return A list of moves.
     */
    public List<Hex.Direction> moves() {
        List<Hex.Direction> directionChanges = new ArrayList<>();
        while (!isEmpty()) {
            QueueStep queueStep = pollBase();
            if (queueStep instanceof MoveAction) {
                directionChanges.add(((MoveAction) queueStep).getDirection());
            } else if (queueStep instanceof MoveToRoutine) {
                directionChanges.addAll(((MoveToRoutine) queueStep).peekAtAllActions().stream().map(MoveAction::getDirection).collect(Collectors.toList()));
            } else if (queueStep instanceof RepeatRoutine && ((RepeatRoutine) queueStep).getAction() instanceof MoveAction) {
                for (int i = 0; i < ((RepeatRoutine) queueStep).getAmount(); i++) {
                    directionChanges.add(((MoveAction) ((RepeatRoutine) queueStep).getAction()).getDirection());
                }
            } else {
                remove(queueStep);
            }
        }
        return directionChanges;
    }
}
