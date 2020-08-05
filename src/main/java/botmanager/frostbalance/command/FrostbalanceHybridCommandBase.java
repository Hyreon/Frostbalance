package botmanager.frostbalance.command;

import botmanager.frostbalance.Frostbalance;

public abstract class FrostbalanceHybridCommandBase extends FrostbalanceCommandBase {

    public FrostbalanceHybridCommandBase(Frostbalance bot, String[] keywords, AuthorityLevel authorityLevel, Condition... conditions) {
        super(bot, keywords, authorityLevel, conditions);
    }

    @Override
    public void execute(CommandContext eventWrapper, String[] params) {
        runHybrid(eventWrapper, params);
    }

    protected abstract void runHybrid(CommandContext eventWrapper, String... params);

}
