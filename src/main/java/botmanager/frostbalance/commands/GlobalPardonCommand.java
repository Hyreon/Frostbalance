package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GlobalPardonCommand extends FrostbalanceCommandBase {

    final String[] KEYWORDS = {
            bot.getPrefix() + "pardon"
    };

    public GlobalPardonCommand(BotBase bot) {
        super(bot);
    }

    @Override
    public void run(Event genericEvent) {
        GuildMessageReceivedEvent event;
        String message;
        String result = "";
        boolean found = false;

        String targetId;
        User targetUser;

        if (!(genericEvent instanceof GuildMessageReceivedEvent)) {
            return;
        }

        event = (GuildMessageReceivedEvent) genericEvent;
        message = event.getMessage().getContentRaw();

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
            result = "You do not have sufficient authority to globally pardon a player.";
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        targetId = Utilities.findUserId(event.getGuild(), message);
        targetUser = event.getJDA().getUserById(targetId);

        if (targetUser == null) {
            result += "Could not find user " + message + ".";
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        boolean success = bot.globallyPardonUser(targetUser);

        if (success) {
            result += "Pardoned player '" + targetUser.getAsMention() + "' from all servers.";
        } else {
            result += "Unable to pardon '" + targetUser.getAsMention() + "'. They are not banned.";
        }

        Utilities.sendGuildMessage(event.getChannel(), result);

    }

    @Override
    public String info() {
        return "**" + bot.getPrefix() + "pardon PLAYER** - pardons a player across all servers.";
    }

}
