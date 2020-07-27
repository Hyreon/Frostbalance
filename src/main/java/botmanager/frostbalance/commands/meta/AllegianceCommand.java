package botmanager.frostbalance.commands.meta;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.generic.GenericMessageReceivedEventWrapper;
import botmanager.frostbalance.menu.AllegianceMenu;

public class AllegianceCommand extends FrostbalanceHybridCommandBase {

    public AllegianceCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "allegiance"
        }, AuthorityLevel.GENERIC);
    }

    @Override
    protected void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String message) {

        new AllegianceMenu(bot, AllegianceMenu.Cause.CHANGE).send(eventWrapper.getChannel(), eventWrapper.getAuthor());

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return null;
    }
}
