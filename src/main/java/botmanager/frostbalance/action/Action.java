package botmanager.frostbalance.action;

import botmanager.frostbalance.grid.PlayerCharacter;

public abstract class Action implements QueueStep {

    PlayerCharacter playerCharacter;

    public Action(PlayerCharacter character) {
        this.playerCharacter = character;
    }

    @Override
    public QueueStep simulate() {
        return this;
    }

}
