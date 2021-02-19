package botmanager.frostbalance.action;

import botmanager.frostbalance.action.actions.Action;
import botmanager.frostbalance.action.actions.MoveAction;
import botmanager.frostbalance.action.routine.MoveToRoutine;
import botmanager.frostbalance.action.routine.RepeatRoutine;
import botmanager.frostbalance.action.routine.Routine;
import botmanager.frostbalance.grid.Containable;
import botmanager.frostbalance.grid.Container;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.coordinate.Hex;

import java.util.*;

public class ActionQueue extends LinkedList<QueueStep> implements Container, Containable<PlayerCharacter> {

    transient PlayerCharacter playerCharacter;

    public ActionQueue(PlayerCharacter character) {
        super();
        this.playerCharacter = character;
    }

    /**
     * Used for json deserialization (and nothing else.)
     */
    public ActionQueue() {
        super();
        this.playerCharacter = null;
    }

    @Override
    public Action poll() {
        QueueStep nextStep = peekBase();
        if (nextStep instanceof Action) {
            return (Action) super.poll();
        } else if (nextStep instanceof Routine) {
            Action nextAction = ((Routine) nextStep).pollAction();
            if (nextAction == null) {
                pollBase();
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

    /**
     * By default, peek will get an Action rather then a full QueueStep.
     * @return
     */
    @Override
    public Action peek() {
        return peek(0);
    }

    /**
     * Get the next action of the given queueStep.
     * @param i The queueStep to examine (0 is the next, 1 is after that, etc)
     * @return
     */
    private Action peek(int i) {
        QueueStep nextStep;
        try {
            nextStep = get(size() - 1 - i);
        } catch (IndexOutOfBoundsException e) {
            nextStep = null;
        }
        if (nextStep instanceof Action) {
            return (Action) nextStep;
        } else if (nextStep instanceof Routine) {
            Action nextAction = ((Routine) nextStep).peekAction();
            if (nextAction == null) {
                return peek(i - 1);
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

    public QueueStep peekBase() { return super.peek(); }

    /**
     * Creates a clone of this ActionQueue that does not modify the existing game state (unless you want it to).
     * @return
     */
    public ActionQueue simulation() {
        //TODO make this safer by not allowing write access to the player character.
        //don't know even where to start with that one
        return simulation(size());
    }

    public ActionQueue simulation(int until) {
        ActionQueue simulation = new ActionQueue(playerCharacter);
        for (QueueStep step : subList(0, until)) {
            simulation.add(step.simulate());
        }
        return simulation;
    }

    /**
     * Destroys the current simulation (without firing anything) to get a list of moves.
     * Should only be run on a simulation.
     * @return A list of moves.
     */
    public List<Hex.Direction> moves() {
        System.out.println("Mapping to moveset");
        List<Hex.Direction> directionChanges = new ArrayList<>();
        Hex lastWaypoint = playerCharacter.getLocation();
        for (Hex waypoint : waypoints(true)) {
            directionChanges.addAll(waypoint.subtract(lastWaypoint).crawlDirections());
            lastWaypoint = waypoint;
        }
        System.out.println("Got moves!");
        return directionChanges;
    }

    /**
     * Consumes the action queue to get a list of waypoints.
     * Should only ever be run on a simulation.
     * @return
     */
    public List<Hex> waypoints(boolean includeSoft) {
        LinkedList<Hex> locations = new LinkedList<>();
        locations.add(playerCharacter.getLocation());
        while (!isEmpty()) {
            QueueStep queueStep = pollBase();
            if (queueStep instanceof MoveAction) {
                locations.add(locations.getLast().move(((MoveAction) queueStep).getDirection()));
            } else if (queueStep instanceof MoveToRoutine) {
                if (includeSoft) {
                    System.out.println("Getting soft waypoints for " + queueStep);
                    locations.addAll(((MoveToRoutine) queueStep).getSoftWaypoints());
                }
                locations.add(((MoveToRoutine) queueStep).getDestination());
            } else if (queueStep instanceof RepeatRoutine && ((RepeatRoutine) queueStep).getAction() instanceof MoveAction) {
                locations.add(locations.getLast().move(((MoveAction) ((RepeatRoutine) queueStep).getAction()).getDirection(), ((RepeatRoutine) queueStep).getAmount()));
            } else {
                remove(queueStep);
            }
        }
        return locations;
    }

    @Override
    public void adopt() {
        Iterator<QueueStep> iter = iterator();
        while (iter.hasNext()) {
            QueueStep step = iter.next();
            step.setParent(this);
        }
    }

    @Override
    public void setParent(PlayerCharacter parent) {
        this.playerCharacter = parent;
    }

    /**
     * A clarification that the QueueStep itself is being sent, not the Action.
     * @return The last QueueStep currently en route
     */
    private QueueStep popBase() {
        return super.poll();
    }

    public PlayerCharacter getCharacter() {
        return playerCharacter;
    }
}
