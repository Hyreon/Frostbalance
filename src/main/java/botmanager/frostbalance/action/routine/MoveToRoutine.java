package botmanager.frostbalance.action.routine;

import botmanager.Utilities;
import botmanager.frostbalance.action.ActionQueue;
import botmanager.frostbalance.action.QueueStep;
import botmanager.frostbalance.action.actions.MoveAction;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.coordinate.Hex;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class MoveToRoutine extends Routine {

    /**
     * All intermediate waypoints.
     * These are skipped if a faster route is found that avoids them.
     */
    List<Hex> softWaypoints = new ArrayList<>();

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
            softWaypoints = previousRoutine.getSoftWaypoints();
            previousDestination = previousRoutine.getDestination();
            softWaypoints.add(previousDestination);
        }
        destination = previousDestination.move(direction, amount);
        updateWaypoints();
    }

    private void updateWaypoints() {
        Hex startLocation = queue.getCharacter().getLocation();
        List<Long> distances = new ArrayList<>();
        Hex lastLocation = startLocation;
        for (Hex hex : softWaypoints) {
            distances.add(lastLocation.minimumDistance(hex));
            lastLocation = hex;
        }
        while (!distances.isEmpty() && startLocation.minimumDistance(getDestination()) < Utilities.addNumericalList(distances)) {
            System.out.println("Removing soft waypoint at " + softWaypoints.remove(softWaypoints.size() - 1).toString());
            distances.remove(distances.size() - 1);
        }
    }

    private List<Hex> getSoftWaypoints() {
        return new ArrayList<>(softWaypoints);
    }

    @Override
    public MoveAction pollAction() {

        return peekAction();

    }


    @Override
    public MoveAction peekAction() {
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
