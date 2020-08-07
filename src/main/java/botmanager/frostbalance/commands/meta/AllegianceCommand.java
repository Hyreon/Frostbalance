package botmanager.frostbalance.commands.meta;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommandBase;
import botmanager.frostbalance.command.GuildCommandContext;
import botmanager.frostbalance.menu.AllegianceMenu;

public class AllegianceCommand extends FrostbalanceGuildCommandBase {

    public AllegianceCommand(Frostbalance bot) {
        super(bot, new String[] {
                "allegiance"
        }, AuthorityLevel.GENERIC);
    }

    @Override
    protected void executeWithGuild(GuildCommandContext context, String... params) {

        new AllegianceMenu(bot, AllegianceMenu.Cause.CHANGE)
                .send(context.getChannel(), context.getJDAUser());

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + aliases[0] + "** - shows the allegiance menu, allowing you to change your national loyalty.";
    }
}
