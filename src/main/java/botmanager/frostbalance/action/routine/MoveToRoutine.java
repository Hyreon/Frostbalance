package botmanager.frostbalance.action.routine;

import botmanager.frostbalance.action.ActionQueue;
import botmanager.frostbalance.action.actions.MoveAction;
import botmanager.frostbalance.action.QueueStep;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.coordinate.Hex;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class MoveToRoutine extends Routine {

    Hex destination;

    /**
     * Creates a new MoveToRoutine.
     * @param destination The destination for the moveToRoutine
     */
    public MoveToRoutine(PlayerCharacter mobile, Hex destination) {
        setParent(mobile.getActionQueue());
        this.destination = destination;
    }

    public MoveToRoutine(ActionQueue queue, Hex destination) {
        setParent(queue);
        this.destination = destination;
    }

    public MoveToRoutine(ActionQueue queue, Hex.Direction direction, int amount) {
        setParent(queue);
        MoveToRoutine previousRoutine = (MoveToRoutine) queue.displace(this.getClass());
        Hex previousDestination;
        if (previousRoutine == null) {
            System.out.println("Adding after last destination!");
            List<Hex> waypoints = queue.simulation().waypoints();
            previousDestination = waypoints.get(waypoints.size() - 1); //last waypoint
        } else {
            System.out.println("Replacing last destination!");
            previousDestination = previousRoutine.getDestination();
        }
        destination = previousDestination.move(direction, amount);
    }

    @Override
    public MoveAction pollAction() {

        return peekAction();

    }


    @Override
    public MoveAction peekAction() {
        //TODO find the root cause of an exception that can occur here
        if (destination.equals(queue.getCharacter().getLocation())) {
            return null;
        } else {
            return new MoveAction(queue.getCharacter(), destination.subtract(queue.getCharacter().getLocation()).crawlDirection());
        }

    }

    public Queue<MoveAction> peekAtAllActions() {
        return peekAtAllActions(queue.indexOf(this));
    }

    /**
     * Guesses what the actions of this routine will be based on the state of the mobile.
     * @return True if the routine completed; false if actions were already queued
     */
    public Queue<MoveAction> peekAtAllActions(int simulationStep) {

        Queue<MoveAction> moveActions = new LinkedBlockingQueue<>();

        ActionQueue simulation = queue.simulation(simulationStep);
        List<Hex> locations = simulation.waypoints();
        Hex startLocation;
        if (locations.isEmpty()) {
            startLocation = queue.getCharacter().getLocation();
        } else {
            startLocation = locations.get(locations.size() - 1); //last location
        }

        for (Hex.Direction nextDirection : getDestination().subtract(startLocation).crawlDirections()) {
            moveActions.add(new MoveAction(queue.getCharacter(), nextDirection));
        }

        return moveActions;

    }

    public List<Hex.Direction> directionsFrom(Hex location) {
        return new LinkedList<>(getDestination().subtract(location).crawlDirections());
    }

    public Hex getDestination() {
        return destination;
    }

    @Override
    public QueueStep simulate() {
        return new MoveToRoutine(this.queue, this.destination);
    }
}
