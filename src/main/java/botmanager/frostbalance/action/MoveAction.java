package botmanager.frostbalance.action;

import botmanager.frostbalance.checks.FrostbalanceException;
import botmanager.frostbalance.checks.PassableTileValidator;
import botmanager.frostbalance.checks.ValidationTests;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.coordinate.Hex;

public class MoveAction extends Action {

    Hex.Direction direction;

    public MoveAction(PlayerCharacter character, Hex.Direction direction) {
        super(character);
        this.direction = direction;
    }

    @Override
    public int moveCost() {
        return 1;
    }

    @Override
    public void doAction() throws FrostbalanceException {

        new ValidationTests().throwIfAny(
                new PassableTileValidator(playerCharacter.getTile().getNeighbor(direction))
        );

        playerCharacter.setLocation(playerCharacter.getLocation().move(direction));

    }

    public Hex.Direction getDirection() {
        return direction;
    }
}
