package botmanager.frostbalance.action.routine;

import botmanager.Utilities;
import botmanager.frostbalance.action.ActionQueue;
import botmanager.frostbalance.action.QueueStep;
import botmanager.frostbalance.action.actions.MoveAction;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.coordinate.Hex;

import java.util.ArrayList;
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

    public MoveToRoutine(ActionQueue queue, Hex destination, List<Hex> softWaypoints) {
        setParent(queue);
        this.destination = destination;
        this.softWaypoints = softWaypoints;
    }

    public MoveToRoutine(ActionQueue queue, List<Hex> softWaypoints, Hex previousDestination, Hex.Direction direction, int amount) {
        setParent(queue);
        this.softWaypoints = softWaypoints;
        this.softWaypoints.add(previousDestination);
        destination = previousDestination.move(direction, amount);
        updateWaypoints();
    }

    public MoveToRoutine(ActionQueue queue, Hex previousDestination, Hex.Direction direction, int amount) {
        setParent(queue);
        this.softWaypoints = new ArrayList<>();
        this.softWaypoints.add(previousDestination);
        destination = previousDestination.move(direction, amount);
        updateWaypoints();
    }

    public MoveToRoutine(ActionQueue queue, Hex.Direction direction, int amount) {
        setParent(queue);
        this.softWaypoints = new ArrayList<>();
        destination = queue.getCharacter().getLocation().move(direction, amount);
    }

    private void updateWaypoints() {
        Hex startLocation = queue.getCharacter().getLocation();
        List<Long> distances = new ArrayList<>();
        Hex lastLocation = startLocation;
        for (Hex hex : getSoftWaypoints()) {
            distances.add(lastLocation.minimumDistance(hex));
            lastLocation = hex;
        }
        distances.add(lastLocation.minimumDistance(destination));
        long minDistance = startLocation.minimumDistance(getDestination());
        long actualDistance = Utilities.addNumericalList(distances);
        while (!distances.isEmpty() && minDistance < actualDistance) {
            System.out.println(minDistance + " IS MIN DISTANCE, CURRENT IS " + actualDistance);
            System.out.println("Removing soft waypoint at " + getSoftWaypoints().remove(getSoftWaypoints().size() - 1));
            distances.remove(distances.size() - 1);

            if (distances.size() == 1) {
                break; //there are no more soft waypoints. the route will now be manually drawn
            }

            //must be equal to the distance from the destination to the last waypoint
            distances.set(distances.size() - 1, getSoftWaypoints().get(getSoftWaypoints().size() - 1).minimumDistance(getDestination()));
            minDistance = startLocation.minimumDistance(getDestination());
            actualDistance = Utilities.addNumericalList(distances);
        }
        System.out.println(minDistance + " IS MIN DISTANCE, CURRENT IS " + actualDistance);
        System.out.println("Getting soft waypoints, after update, for " + this);
        readSoftWaypoints();
    }

    /**
     * Returns a new list containing all soft waypoints. Does not include the final destination.
     */
    public List<Hex> getSoftWaypoints() {
        if (softWaypoints == null) {
            softWaypoints = new ArrayList<>();
        }
        return softWaypoints;
    }

    /**
     * Returns a new list containing all soft waypoints. Does not include the final destination.
     */
    public List<Hex> readSoftWaypoints() {
        System.out.println("SOFT WAYPOINTS:" + getSoftWaypoints());
        return new ArrayList<>(getSoftWaypoints());
    }

    @Override
    public MoveAction pollAction() {

        return peekAction();

    }


    @Override
    public MoveAction peekAction() {
        while (!getSoftWaypoints().isEmpty() && getSoftWaypoints().get(0).equals(queue.getCharacter().getLocation())) {
            System.out.println("Removing soft waypoint " + getSoftWaypoints().remove(0));
        }
        if (destination.equals(queue.getCharacter().getLocation())) {
            return null;
        } else if (getSoftWaypoints().isEmpty()) {
            return new MoveAction(queue.getCharacter(), destination.subtract(queue.getCharacter().getLocation()).crawlDirection());
        } else {
            return new MoveAction(queue.getCharacter(), getSoftWaypoints().get(0).subtract(queue.getCharacter().getLocation()).crawlDirection());
        }

    }

    public Queue<MoveAction> peekAtAllActions() {
        return peekAtAllActions(queue.indexOf(this));
    }

    /**
     * Guesses what the actions of this routine will be based on the state of the mobile.
     */
    public Queue<MoveAction> peekAtAllActions(int simulationStep) {

        Queue<MoveAction> moveActions = new LinkedBlockingQueue<>();

        ActionQueue simulation = queue.simulation(simulationStep);
        List<Hex> locations = simulation.waypoints(true);
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

    public Hex getDestination() {
        return destination;
    }

    @Override
    public QueueStep simulate() {
        return new MoveToRoutine(this.queue, this.destination, this.getSoftWaypoints());
    }

    @Override
    public String displayStep() {
        String waypoints = "";
        if (!getSoftWaypoints().isEmpty()) {
            waypoints = " {Favored route: " + getSoftWaypoints().toString() + "}";
        }
        return "**Move to " + getDestination().getCoordinates(queue.getCharacter()) + "**";
    }

}
