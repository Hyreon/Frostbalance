package botmanager.frostbalance.commands;

import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.generic.GenericMessageReceivedEventWrapper;
import botmanager.generic.BotBase;

//TODO
public class FlagCommand extends FrostbalanceHybridCommandBase {


    public FlagCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "flag"
        }, AuthorityLevel.BOT_ADMIN);
    }

    @Override
    public void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String message) {
        String result = "";

        result += "This server has no flags, " + eventWrapper.getMember().getEffectiveName() + "!";

        eventWrapper.sendResponse(result);

    }

    @Override
    public String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (authorityLevel.hasAuthority(AuthorityLevel.BOT_ADMIN)) {
            return "**" + bot.getPrefix() + "flag FLAG** - toggle a debug flag to this server\n" +
                    "**" + bot.getPrefix() + "flag** - view debug flags for this server";
        } else if (authorityLevel.hasAuthority(AuthorityLevel.GUILD_ADMIN)) {
            //anyone can do it, but lower ranks would be wholly uninterested
            return "**" + bot.getPrefix() + "flag** - view debug flags for this server";
        } else {
            return null;
        }
    }
}
