package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.frostbalance.history.TerminationCondition;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class InterveneCommand extends FrostbalanceCommandBase {

    final String[] KEYWORDS = {
            bot.getPrefix() + "reset"
    };

    public InterveneCommand(BotBase bot) {
        super(bot);
    }

    @Override
    public void run(Event genericEvent) {

        GuildMessageReceivedEvent event;
        String message;
        String id;
        String result;
        Member currentOwner;
        boolean found = false;

        if (!(genericEvent instanceof GuildMessageReceivedEvent)) {
            return;
        }

        event = (GuildMessageReceivedEvent) genericEvent;
        message = event.getMessage().getContentRaw();
        id = event.getAuthor().getId();

        for (String keyword : KEYWORDS) {
            if (message.equalsIgnoreCase(keyword)) {
                message = message.replace(keyword, "");
                found = true;
                break;
            } else if (message.startsWith(keyword + " ")) {
                message = message.replace(keyword + " ", "");
                found = true;
                break;
            }
        }

        if (!found) {
            return;
        }

        if (!event.getMember().getRoles().contains(bot.getSystemRole(event.getGuild()))) {
            result = "You do not have sufficient authority to intervene.";
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        currentOwner = bot.getOwner(event.getGuild());

        if (currentOwner == null) {
            result = "You can't reset if there's an owner already.";

        }

        bot.endRegime(event.getGuild(), TerminationCondition.RESET);

        result = currentOwner.getAsMention() + "has been removed as leader by administrative action.";

        bot.softReset(event.getGuild());

        result += "\nAll non-system bans and player roles have been reset.";

        Utilities.sendGuildMessage(event.getChannel(), result);

    }


    @Override
    public String info() {
        return ""
                + "**" + bot.getPrefix() + "reset** - Remove the current owner, unban all players, and reset all non-system roles. " +
                "The current owner can't become owner until a new owner is in place.";
    }

}
