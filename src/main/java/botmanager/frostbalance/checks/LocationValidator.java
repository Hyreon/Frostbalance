package botmanager.frostbalance.checks;

import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.coordinate.Hex;

public class LocationValidator implements Check {

    Hex firstLocation;
    Hex secondLocation;

    public LocationValidator(Hex firstLocation, Hex secondLocation) {
        this.firstLocation = firstLocation;
        this.secondLocation = secondLocation;
    }

    public LocationValidator(PlayerCharacter character, Hex secondLocation) {
        this(character.getLocation(), secondLocation);
    }

    @Override
    public boolean validate() {
        return firstLocation.equals(secondLocation);
    }

    @Override
    public String displayCondition() {
        return "You must be in the right location.";
    }
}
