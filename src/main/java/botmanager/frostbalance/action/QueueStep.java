package botmanager.frostbalance.action;

import botmanager.frostbalance.checks.FrostbalanceException;
import botmanager.frostbalance.grid.Containable;

public interface QueueStep extends Containable<ActionQueue> {

    int moveCost();

    void doAction() throws FrostbalanceException;

    QueueStep simulate();

    default QueueStep refreshed() {
        return this;
    }

    default String displayStep() { return toString(); }
}
