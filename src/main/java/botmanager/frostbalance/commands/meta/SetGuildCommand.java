package botmanager.frostbalance.commands.meta;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.command.GenericMessageReceivedEventWrapper;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;

public class SetGuildCommand extends FrostbalanceHybridCommandBase {

    public SetGuildCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "guild"
        }, AuthorityLevel.GENERIC, Condition.PRIVATE);
    }

    @Override
    protected void runHybrid(GenericMessageReceivedEventWrapper event, String... params) {

        String name;
        List<String> resultLines = new ArrayList<>();

        if (params.length == 0) {
            event.getBotUser().resetDefaultGuild();

            resultLines.add("Removed default guild.");
            event.sendResponse(resultLines);
            return;
        }

        name = Utilities.combineArrayStopAtIndex(params, params.length);
        List<Guild> guilds = event.getJDA().getGuildsByName(name, true);

        if (guilds.isEmpty()) {
            resultLines.add("Couldn't find guild '" + name + "'.");
            event.sendResponse(resultLines);
            return;
        }

        event.getBotUser().setDefaultGuildId(guilds.get(0).getId());

        resultLines.add("Set default guild to **" + guilds.get(0).getName() + "**.");
        event.sendResponse(resultLines);

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (isPublic) return null;
        else {
            return "**" + bot.getPrefix() + "guild GUILDNAME** - sets your default guild when running commands through PM." + "\n" +
                    "**" + bot.getPrefix() + "guild** - resets your default guild.";
        }
    }
}
