package botmanager.frostbalance.commands;

import botmanager.frostbalance.OptionFlag;
import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.generic.GenericMessageReceivedEventWrapper;
import botmanager.generic.BotBase;

import java.util.stream.Collectors;

//TODO
public class FlagCommand extends FrostbalanceHybridCommandBase {


    public FlagCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "flag"
        }, AuthorityLevel.GUILD_ADMIN);
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

        if (stringFlag.isEmpty()) {
            result += "Flags on this server: \n";
            result += String.join("\n",
                    bot.getDebugFlags(eventWrapper.getGuild())
                            .stream()
                            .map(o -> o.toString())
                            .collect(Collectors.toList()));
        } else {
            try {
                OptionFlag toggledFlag = OptionFlag.valueOf(stringFlag);
                if (bot.flipFlag(eventWrapper.getGuild(), toggledFlag)) {
                    result += "Enabled " + toggledFlag;
                } else {
                    result += "Disabled " + toggledFlag;
                }
            } catch (IllegalArgumentException e) {
                result += "The flag '" + stringFlag + "' doesn't exist.";
            }
        }

        eventWrapper.sendResponse(result);

    }

    @Override
    public String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (authorityLevel.hasAuthority(AuthorityLevel.BOT_ADMIN)) {
            return "**" + bot.getPrefix() + "flag FLAG** - toggle a debug flag to this server\n" +
                    "**" + bot.getPrefix() + "flag** - view debug flags for this server";
        } else if (authorityLevel.hasAuthority(AuthorityLevel.GUILD_ADMIN)) {
            return "**" + bot.getPrefix() + "flag** - view debug flags for this server";
        } else {
            return null;
        }
    }
}
