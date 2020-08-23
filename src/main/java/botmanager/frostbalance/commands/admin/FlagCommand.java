package botmanager.frostbalance.commands.admin;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.ContextLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommand;
import botmanager.frostbalance.command.GuildCommandContext;
import botmanager.frostbalance.menu.FlagMenu;

public class FlagCommand extends FrostbalanceGuildCommand {


    public FlagCommand(Frostbalance bot) {
        super(bot, new String[] {
                "flags",
                "settings"
        }, AuthorityLevel.GENERIC, ContextLevel.ANY);
    }

    @Override
    public void executeWithGuild(GuildCommandContext context, String... params) {
        String result = "";

        new FlagMenu(getBot(), context).send(context.getChannel(), context.getAuthor());

    }

    @Override
    public String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (authorityLevel.hasAuthority(AuthorityLevel.GUILD_ADMIN)) {
            return "**" + getBot().getPrefix() + "settings** - view/edit settings for this server";
        } else {
            return "**" + getBot().getPrefix() + "settings** - view settings for this server";
        }
    }
}
