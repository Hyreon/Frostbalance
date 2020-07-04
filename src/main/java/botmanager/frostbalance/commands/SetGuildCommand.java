package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class SetGuildCommand extends FrostbalanceCommandBase {

    final String[] KEYWORDS = {
            bot.getPrefix() + "guild"
    };

    public SetGuildCommand(BotBase bot) {
        super(bot);
    }

    @Override
    public void run(Event genericEvent) {
        MessageReceivedEvent event;
        String[] words;
        String message;
        String name;
        String id;
        String result = "";
        boolean found = false;

        if (!(genericEvent instanceof MessageReceivedEvent)) {
            return;
        }

        event = (MessageReceivedEvent) genericEvent;
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

        if (genericEvent instanceof GuildMessageReceivedEvent) {
            result = "This command only works through PM.";
            Utilities.sendPrivateMessage(event.getAuthor(), result);
            return;
        }

        words = message.split(" ");

        if (message.isEmpty()) {
            bot.resetUserDefaultGuild(event.getAuthor());

            result += "Removed default guild.";
            Utilities.sendPrivateMessage(event.getAuthor(), result);
            return;
        }

        name = combineArrayStopAtIndex(words, words.length);
        List<Guild> guilds = event.getJDA().getGuildsByName(name, true);

        if (guilds.isEmpty()) {
            Utilities.sendPrivateMessage(event.getAuthor(), "Couldn't find guild '" + name + "'.");
            return;
        }

        bot.setUserDefaultGuild(event.getAuthor(), guilds.get(0));

        result += "Set default guild to **" + guilds.get(0).getName() + "**.";

        Utilities.sendPrivateMessage(event.getAuthor(), result);

    }

    public String combineArrayStopAtIndex(String[] array, int index) {
        String result = "";

        for (int i = 0; i < index; i++) {
            result += array[i];

            if (i + 1 != index) {
                result += " ";
            }
        }

        return result;
    }

    @Override
    public String info() {
        return "**" + bot.getPrefix() + "guild GUILDNAME** in PM - sets your default guild when running commands through PM." + "\n" +
                "**" + bot.getPrefix() + "guild** in PM - resets your default guild.";
    }

}
