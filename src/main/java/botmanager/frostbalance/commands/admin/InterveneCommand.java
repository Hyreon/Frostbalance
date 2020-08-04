package botmanager.frostbalance.commands.admin;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceSplitCommandBase;
import botmanager.frostbalance.data.TerminationCondition;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class InterveneCommand extends FrostbalanceSplitCommandBase {

    public InterveneCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "reset"
        }, AuthorityLevel.GUILD_ADMIN);
    }

    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {

        String id;
        String result;
        Member currentOwner;

        id = event.getAuthor().getId();

        currentOwner = bot.getOwner(event.getGuild());

        if (currentOwner == null) {
            result = "You can't reset if there's no owner.";
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        bot.endRegime(event.getGuild(), TerminationCondition.RESET);

        result = currentOwner.getAsMention() + " has been removed as leader by administrative action.";

        bot.softReset(event.getGuild());

        result += "\nAll non-system bans and player roles have been reset.";

        Utilities.sendGuildMessage(event.getChannel(), result);

    }


    @Override
    public String publicInfo(AuthorityLevel authorityLevel) {
        return ""
                + "**" + bot.getPrefix() + "reset** - Remove the current owner, unban all players, and reset all non-system roles. " +
                "The current owner can't become owner until a new owner is in place.";
    }


    @Override
    public String privateInfo(AuthorityLevel authorityLevel) {
        return null;
    }

}
