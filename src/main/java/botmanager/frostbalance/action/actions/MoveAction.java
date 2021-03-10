package botmanager.frostbalance.action.actions;

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
                new PassableTileValidator(queue.getCharacter().getTile().getNeighbor(direction))
        );

        queue.getCharacter().setLocation(queue.getCharacter().getLocation().move(direction));

    }

    public Hex.Direction getDirection() {
        return direction;
    }

    @Override
    public String displayStep() {
        return "Move " + getDirection().name();
    }
}
