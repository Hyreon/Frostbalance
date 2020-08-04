package botmanager.frostbalance.commands.meta;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceCommandBase;
import botmanager.frostbalance.command.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.command.GenericMessageReceivedEventWrapper;

/**
 *
 * @author MC_2018 <mc2018.git@gmail.com>
 */
public class HelpCommand extends FrostbalanceHybridCommandBase {

    public HelpCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "help"
        }, AuthorityLevel.GENERIC);
    }

    @Override
    protected void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String... params) {
        final String[] result = {"__**Frostbalance**__\n\n"};

        for (FrostbalanceCommandBase command : bot.getCommands()) {
            command.getInfo(eventWrapper).ifPresent(info -> result[0] += info + "\n");
        }

        eventWrapper.sendResponse(result[0]);
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
