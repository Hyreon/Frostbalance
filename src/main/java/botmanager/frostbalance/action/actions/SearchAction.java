package botmanager.frostbalance.action.actions;

import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.resource.ResourceData;

public class SearchAction extends Action {

    public SearchAction(PlayerCharacter character) {
        super(character);
    }

    @Override
    public int moveCost() {
        return 1;
    }

    @Override
    public void doAction() {

        PlayerCharacter character = queue.getCharacter();
        ResourceData resourceData = character.getTile().getResourceData();
        int startingResources = resourceData.numResources();
        long attempts = resourceData.getAttempts();
        int progress = resourceData.getProgress();
        boolean success = resourceData.search(false);
        if (success && resourceData.getProgress() == 0) { //progress reset
            if (startingResources == resourceData.numResources()) {
                character.getUser().sendNotification(character.getMap().getGameNetwork().guildWithAllegiance(character.getNation()),
                        "Your search at " + character.getTile().getLocation().getCoordinates(character.getUser().getUserOptions().getCoordSys()) +
                                " has revealed that this tile is exhausted (after **" + attempts + "** attempts). There is nothing left to be found.");
            } else {
                queue.getCharacter().getUser().sendNotification(character.getMap().getGameNetwork().guildWithAllegiance(character.getNation()),
                        "Your search at " + character.getTile().getLocation().getCoordinates(character.getUser().getUserOptions().getCoordSys()) +
                                " has revealed *" + resourceData.recentDeposit() + "* (after **" + attempts + "** attempts)! You can now set up a gatherer for this resource.");
            }
        }

    }

}
