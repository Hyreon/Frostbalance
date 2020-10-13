package botmanager.frostbalance.action.routine;

import botmanager.frostbalance.action.Action;
import botmanager.frostbalance.action.ActionQueue;
import botmanager.frostbalance.action.MoveAction;
import botmanager.frostbalance.action.QueueStep;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.TileObject;
import botmanager.frostbalance.grid.coordinate.Hex;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

public class MoveToRoutine extends Routine {

    Hex destination;

    /**
     * Creates a new MoveToRoutine.
     * @param destination The destination for the moveToRoutine
     */
    public MoveToRoutine(PlayerCharacter mobile, Hex destination) {
        this.mobile = mobile;
        this.destination = destination;
    }

    @Override
    public MoveAction pollAction() {

        return new MoveAction(mobile, destination.subtract(mobile.getLocation()).crawlDirection());

    }

    /**
     * Guesses what the actions of this routine will be based on the state of the mobile.
     * @param character The character to do the routine on
     * @return True if the routine completed; false if actions were already queued
     */
    public Queue<Action> peekAtAllActions(TileObject character) {

        Queue<Action> actions = new PriorityQueue<>();

        Hex location = character.getLocation();

        if (!character.getActionQueue().isEmpty()) {
            //TODO add simulation
            ActionQueue queue = character.getActionQueue().simulator();
            while (!queue.isEmpty()) {
                Action action = queue.poll();
                if (action instanceof MoveAction) {
                    location = location.move(((MoveAction) action).getDirection());
                }
            }
        }

        if (!location.equals(destination)) {

            Hex directions = destination.subtract(location);
            Iterator<Hex.Direction> steps = directions.crawlDirections();
            while (steps.hasNext()) {
                Hex.Direction direction = steps.next();
                actions.add(new MoveAction(mobile, direction));
            }

        }

        return actions;

    }

    public Hex getDestination() {
        return destination;
    }

    @Override
    public QueueStep simulate() {
        return new MoveToRoutine(this.mobile, this.destination);
    }
}
