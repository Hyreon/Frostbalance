package botmanager.frostbalance.action.actions;

import botmanager.frostbalance.action.ActionQueue;
import botmanager.frostbalance.action.QueueStep;
import botmanager.frostbalance.grid.PlayerCharacter;

public abstract class Action implements QueueStep {

    transient ActionQueue queue;

    public Action(PlayerCharacter character) {
        setParent(character.getActionQueue());
    }

    @Override
    public QueueStep simulate() {
        return this;
    }

    @Override
    public void setParent(ActionQueue queue) {
        this.queue = queue;
    }

}
