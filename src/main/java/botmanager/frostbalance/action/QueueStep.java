package botmanager.frostbalance.action;

import botmanager.frostbalance.checks.FrostbalanceException;
import botmanager.frostbalance.grid.Containable;

public interface QueueStep extends Containable<ActionQueue> {

    int moveCost();

    void doAction() throws FrostbalanceException;

    QueueStep simulate();
}
