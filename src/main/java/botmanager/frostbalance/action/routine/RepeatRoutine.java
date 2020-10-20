package botmanager.frostbalance.action.routine;

import botmanager.frostbalance.action.actions.Action;
import botmanager.frostbalance.action.QueueStep;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class RepeatRoutine<A extends Action> extends Routine {

    A action;
    int amount;

    public RepeatRoutine(A action, int amount) {

        this.action = action;
        this.amount = amount;

    }

    public Action pollAction() {
        amount--;
        if (amount <= 0) return null;
        else return action;
    }

    @Override
    public Queue<A> peekAtAllActions() {

        Queue<A> queue = new LinkedBlockingQueue<>();

        for (int i = 0; i < amount; i++) {
            queue.add(action);
        }

        return queue;
    }

    @Override
    public QueueStep simulate() {
        return new RepeatRoutine<>(this.action, this.amount);
    }

    public Action getAction() {
        return action;
    }

    public int getAmount() {
        return amount;
    }

}
