package botmanager.frostbalance.commands.admin;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.generic.GenericMessageReceivedEventWrapper;
import botmanager.frostbalance.menu.FlagMenu;

public class FlagCommand extends FrostbalanceHybridCommandBase {


    public FlagCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "flags",
                bot.getPrefix() + "settings"
        }, AuthorityLevel.GENERIC);
    }

    @Override
    public void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String message) {
        String result = "";
        String stringFlag = message.split(" ")[0];

        if (eventWrapper.getGuild() == null) {
            result += "This command cannot run without a default guild.";
            eventWrapper.sendResponse(result);
            return;
        }

        new FlagMenu(bot, eventWrapper.getGuild()).send(eventWrapper.getChannel(), eventWrapper.getAuthor());

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
