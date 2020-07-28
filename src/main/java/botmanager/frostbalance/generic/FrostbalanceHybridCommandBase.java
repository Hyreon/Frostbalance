package botmanager.frostbalance.generic;

import botmanager.frostbalance.Frostbalance;

public abstract class FrostbalanceHybridCommandBase extends FrostbalanceCommandBase {

    public FrostbalanceHybridCommandBase(Frostbalance bot, String[] keywords, AuthorityLevel authorityLevel) {
        super(bot, keywords, authorityLevel);
    }

    @Override
    public void execute(GenericMessageReceivedEventWrapper eventWrapper, String[] params) {
        runHybrid(eventWrapper, String.join(" ", params));
    }

    protected abstract void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String message);

}
