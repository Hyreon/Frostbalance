package botmanager.frostbalance.commands.meta;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.command.GenericMessageReceivedEventWrapper;
import botmanager.frostbalance.menu.AllegianceMenu;

public class AllegianceCommand extends FrostbalanceHybridCommandBase {

    public AllegianceCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "allegiance"
        }, AuthorityLevel.GENERIC);
    }

    @Override
    protected void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String... params) {

        new AllegianceMenu(bot, AllegianceMenu.Cause.CHANGE)
                .send(eventWrapper.getChannel(), eventWrapper.getAuthor());

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + KEYWORDS[0] + "** - shows the allegiance menu, allowing you to change your national loyalty.";
    }
}
