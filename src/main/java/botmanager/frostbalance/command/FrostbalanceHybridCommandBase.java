package botmanager.frostbalance.command;

import botmanager.frostbalance.Frostbalance;

public abstract class FrostbalanceHybridCommandBase extends FrostbalanceCommandBase {

    public FrostbalanceHybridCommandBase(Frostbalance bot, String[] keywords, AuthorityLevel authorityLevel, Conditions... conditions) {
        super(bot, keywords, authorityLevel, conditions);
    }

    @Override
    public void execute(GenericMessageReceivedEventWrapper eventWrapper, String[] params) {
        runHybrid(eventWrapper, params);
    }

    protected abstract void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String... params);

}
