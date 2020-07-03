package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.frostbalance.generic.FrostbalanceHybridCommandBase;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

/**
 *
 * @author MC_2018 <mc2018.git@gmail.com>
 */
public class HelpCommand extends FrostbalanceHybridCommandBase {

    public HelpCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "help"
        });
    }
    
    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {
        String result = "__**Frostbalance**__\n\n";

        for (FrostbalanceCommandBase command : bot.getCommands()) {
            String info = command.info();

            if (info != null) {
                result += info + "\n";
            }
        }
        
        Utilities.sendGuildMessage(event.getChannel(), result);
    }

    @Override
    public void runPrivate(PrivateMessageReceivedEvent event, String message) {
        String result = "__**Frostbalance**__\n\n";

        for (FrostbalanceCommandBase command : bot.getCommands()) {
            String info = command.info();

            if (info != null) {
                result += info + "\n";
            }
        }

        Utilities.sendPrivateMessage(event.getAuthor(), result);
    }

    @Override
    public String info() {
        return "**" + bot.getPrefix() + "help** - rattles off a bunch of useless commands";
    }
    
}
