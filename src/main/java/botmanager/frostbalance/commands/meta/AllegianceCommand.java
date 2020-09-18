package botmanager.frostbalance.commands.meta;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.ContextLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommand;
import botmanager.frostbalance.command.GuildMessageContext;
import botmanager.frostbalance.menu.AllegianceMenu;

public class AllegianceCommand extends FrostbalanceGuildCommand {

    public AllegianceCommand(Frostbalance bot) {
        super(bot, new String[] {
                "allegiance"
        }, AuthorityLevel.GENERIC, ContextLevel.ANY);
    }

    @Override
    protected void executeWithGuild(GuildMessageContext context, String... params) {

        new AllegianceMenu(getBot(), context, AllegianceMenu.Cause.CHANGE)
                .send(context.getChannel(), context.getAuthor());

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**." + getMainAlias() + "** - shows the allegiance menu, allowing you to change your national loyalty.";
    }
}
