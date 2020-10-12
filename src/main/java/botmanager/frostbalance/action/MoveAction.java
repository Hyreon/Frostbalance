package botmanager.frostbalance.action;

import botmanager.frostbalance.checks.FrostbalanceException;
import botmanager.frostbalance.checks.PassableTileValidator;
import botmanager.frostbalance.checks.ValidationTests;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.coordinate.Hex;

public class MoveAction extends Action {

    Hex.Direction direction;

    public MoveAction(Hex.Direction direction) {
        this.direction = direction;
    }

    @Override
    public void doAction(PlayerCharacter playerCharacter) throws FrostbalanceException {

        new ValidationTests().throwIfAny(
            new PassableTileValidator(playerCharacter.getTile().getNeighbor(direction))
        );

        playerCharacter.setLocation(playerCharacter.getLocation().move(direction));
    }
}
