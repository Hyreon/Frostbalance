package botmanager.frostbalance.action.routine;

import botmanager.frostbalance.action.Action;
import botmanager.frostbalance.action.QueueStep;
import botmanager.frostbalance.grid.TileObject;

import java.util.PriorityQueue;
import java.util.Queue;

public class RepeatRoutine extends Routine {

    Action action;
    int amount;

    public RepeatRoutine(Action action, int amount) {

        this.action = action;
        this.amount = amount;

    }

    public Action pollAction() {
        amount--;
        if (amount >= 0) return null;
        else return action;
    }

    @Override
    public Queue<Action> peekAtAllActions(TileObject character) {

        Queue<Action> queue = new PriorityQueue<>();

        for (int i = 0; i < amount; i++) {
            queue.add(action);
        }

        return queue;
    }

    @Override
    public QueueStep simulate() {
        return new RepeatRoutine(this.action, this.amount);
    }

    public Action getAction() {
        return action;
    }

    public int getAmount() {
        return amount;
    }
}
