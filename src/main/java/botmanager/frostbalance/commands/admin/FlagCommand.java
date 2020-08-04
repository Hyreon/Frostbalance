package botmanager.frostbalance.commands.admin;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceCommandBase;
import botmanager.frostbalance.command.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.command.GenericMessageReceivedEventWrapper;
import botmanager.frostbalance.menu.FlagMenu;

public class FlagCommand extends FrostbalanceHybridCommandBase {


    public FlagCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "flags",
                bot.getPrefix() + "settings"
        }, AuthorityLevel.GENERIC, FrostbalanceCommandBase.Conditions.GUILD_EXISTS);
    }

    @Override
    public void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String... params) {
        String result = "";

        new FlagMenu(bot, eventWrapper.getGuild().get()).send(eventWrapper.getChannel(), eventWrapper.getAuthor());

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
