package botmanager.frostbalance.action.actions;

import botmanager.frostbalance.grid.PlayerCharacter;

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

        int startingResources = queue.getCharacter().getTile().getResourceData().numResources();
        boolean success = queue.getCharacter().getTile().getResourceData().search(false);
        if (success && queue.getCharacter().getTile().getResourceData().getProgress() == 0) { //progress reset
            if (startingResources == queue.getCharacter().getTile().getResourceData().numResources()) {
                queue.getCharacter().getUser().sendNotification(queue.getCharacter().getMap().getGameNetwork().guildWithAllegiance(queue.getCharacter().getNation()),
                        "Your search at " + queue.getCharacter().getTile().getLocation().getCoordinates(queue.getCharacter().getUser().getUserOptions().getCoordSys()) + " has revealed that this tile is exhausted. There is nothing left to be found.");
            } else {
                queue.getCharacter().getUser().sendNotification(queue.getCharacter().getMap().getGameNetwork().guildWithAllegiance(queue.getCharacter().getNation()),
                        "Your search at " + queue.getCharacter().getTile().getLocation().getCoordinates(queue.getCharacter().getUser().getUserOptions().getCoordSys()) + " has revealed *" + queue.getCharacter().getTile().getResourceData().recentDeposit() + "*! You can now set up a gatherer for this resource.");
            }
        }

    }

}
