package botmanager.frostbalance.commands.admin;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommandBase;
import botmanager.frostbalance.command.GuildCommandContext;
import botmanager.frostbalance.menu.FlagMenu;

public class FlagCommand extends FrostbalanceGuildCommandBase {


    public FlagCommand(Frostbalance bot) {
        super(bot, new String[] {
                "flags",
                "settings"
        }, AuthorityLevel.GENERIC);
    }

    @Override
    public void executeWithGuild(GuildCommandContext context, String... params) {
        String result = "";

        new FlagMenu(bot, context.getGuild()).send(context.getChannel(), context.getAuthor());

    }

    @Override
    public String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (authorityLevel.hasAuthority(AuthorityLevel.GUILD_ADMIN)) {
            return "**" + bot.getPrefix() + "settings** - view/edit settings for this server";
        } else {
            return "**" + bot.getPrefix() + "settings** - view settings for this server";
        }
    }
}
