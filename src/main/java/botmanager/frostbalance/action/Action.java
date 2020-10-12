package botmanager.frostbalance.action;

import botmanager.frostbalance.checks.FrostbalanceException;
import botmanager.frostbalance.grid.PlayerCharacter;

public abstract class Action {


    /**
     *
     * @param playerCharacter The character executing the action
     * @return A list of things that make this action impossible to execute.
     */
    public abstract void doAction(PlayerCharacter playerCharacter) throws FrostbalanceException;

}
