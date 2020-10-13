package botmanager.frostbalance.action;

import botmanager.frostbalance.checks.FrostbalanceException;

public interface QueueStep {

    int moveCost();

    void doAction() throws FrostbalanceException;

    QueueStep simulate();
}
