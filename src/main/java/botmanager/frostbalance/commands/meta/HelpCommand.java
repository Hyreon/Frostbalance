package botmanager.frostbalance.commands.meta;

import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.frostbalance.generic.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.generic.GenericMessageReceivedEventWrapper;
import botmanager.generic.BotBase;

/**
 *
 * @author MC_2018 <mc2018.git@gmail.com>
 */
public class HelpCommand extends FrostbalanceHybridCommandBase {

    public HelpCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "help"
        }, AuthorityLevel.GENERIC);
    }

    @Override
    protected void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String message) {
        String result = "__**Frostbalance**__\n\n";

        for (FrostbalanceCommandBase command : bot.getCommands()) {
            String info = command.getInfo(eventWrapper.getAuthority(), eventWrapper.isPublic());

            if (!command.isAdminOnly() || command.wouldAuthorize(eventWrapper.getGuild(), eventWrapper.getAuthor())) { //hide admin commands
                if (info != null) {
                    result += info + "\n";
                }
            }

        }

        eventWrapper.sendResponse(result);
    }

    @Override
    public String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (isPublic) {
            return "**" + bot.getPrefix() + "help** in public chat - rattles off a bunch of useless commands\n" +
                    "**" + bot.getPrefix() + "help** in DM - lists what commands work in PM";
        } else {
            return "**" + bot.getPrefix() + "help** in DM - rattles off a bunch of useless commands\n" +
                    "**" + bot.getPrefix() + "help** in public chat - lists what commands work in public chat";
        }
    }
}
