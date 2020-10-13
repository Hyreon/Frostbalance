package botmanager.frostbalance.action;

import botmanager.frostbalance.checks.FrostbalanceException;

public interface QueueStep {

    int moveCost();

    /**
     *
     * @param playerCharacter The character executing the action
     */
    void doAction() throws FrostbalanceException;

    QueueStep simulate();
}
