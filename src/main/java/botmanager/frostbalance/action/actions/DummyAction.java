package botmanager.frostbalance.action.actions;

import botmanager.frostbalance.checks.FrostbalanceException;
import botmanager.frostbalance.grid.PlayerCharacter;

public class DummyAction extends Action {

    public DummyAction(PlayerCharacter character) {
        super(character);
    }

    @Override
    public int moveCost() {
        return 0;
    }

    @Override
    public void doAction() throws FrostbalanceException {} //does nothing
}
