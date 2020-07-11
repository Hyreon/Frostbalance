package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

//TODO
public class FlagCommand extends FrostbalanceCommandBase {


    final String[] KEYWORDS = {
            bot.getPrefix() + "flag"
    };

    public FlagCommand(BotBase bot) {
        super(bot);
    }

    @Override
    public void run(Event genericEvent) {
        GuildMessageReceivedEvent event;
        String message;
        String id;
        String result = "";
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

        result += "This server has no flags, " + event.getMember().getEffectiveName() + "!";

        Utilities.sendGuildMessage(event.getChannel(), result);

    }

    @Override
    public String info() {
        return "**" + bot.getPrefix() + "flag FLAG** - apply a flag to this server, if you are staff\n" +
                "**" + bot.getPrefix() + "flag** - view all flags for this server - any user";
    }

}
