package botmanager.frostbalance.commands.meta;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.MessageContext;
import botmanager.frostbalance.command.ContextLevel;
import botmanager.frostbalance.command.FrostbalanceCommand;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;

public class SetGuildCommand extends FrostbalanceCommand {

    public SetGuildCommand(Frostbalance bot) {
        super(bot, new String[] {
                "guild"
        }, AuthorityLevel.GENERIC, ContextLevel.PRIVATE_MESSAGE);
    }

    @Override
    protected void execute(MessageContext context, String... params) {

        String name;
        List<String> resultLines = new ArrayList<>();

        if (params.length == 0) {
            context.getAuthor().resetDefaultGuild();

            resultLines.add("Removed default guild.");
            context.sendMultiLineResponse(resultLines);
            return;
        }

        name = Utilities.combineArrayStopAtIndex(params, params.length);
        List<Guild> guilds = context.getJDA().getGuildsByName(name, true);

        if (guilds.isEmpty()) {
            resultLines.add("Couldn't find guild '" + name + "'.");
            context.sendMultiLineResponse(resultLines);
            return;
        }

        context.getAuthor().setDefaultGuildId(guilds.get(0).getId());

        resultLines.add("Set default guild to **" + guilds.get(0).getName() + "**.");
        context.sendMultiLineResponse(resultLines);

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (isPublic) return null;
        else {
            return "**" + getBot().getPrefix() + "guild GUILDNAME** - sets your default guild when running commands through PM." + "\n" +
                    "**" + getBot().getPrefix() + "guild** - resets your default guild.";
        }
    }
}
