package botmanager.frostbalance.action.routine;

import botmanager.frostbalance.action.actions.Action;
import botmanager.frostbalance.action.QueueStep;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class RepeatRoutine<A extends Action> extends Routine {

    A action;
    private final int initialAmount;
    int amount;

    public RepeatRoutine(A action, int amount) {

        this.action = action;
        this.initialAmount = amount;
        this.amount = amount;

    }

    public Action pollAction() {
        if (amount <= 0) return null;
        else {
            amount--;
            return action;
        }
    }

    @Override
    public Queue<A> peekAtAllActions() {

        Queue<A> queue = new LinkedBlockingQueue<>();

        for (int i = 0; i < amount; i++) {
            queue.add(action);
        }

        return queue;
    }

    /**
     * Simulates the repeat routine as if it were fresh.
     * As simulations are more well-defined we will probably change
     * the definition of this.
     * @return A clone of this repeat routine, as it was originally created.
     */
    @Override
    public QueueStep refreshed() {
        return new RepeatRoutine<>(this.action, initialAmount);
    }

    /**
     * Simulates the repeat routine from where it currently is.
     * @return A clone of this repeat routine, as it was originally created.
     */
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

    @Override
    public String displayStep() {
        return getAction().displayStep() + " x" + getAmount();
    }

}
