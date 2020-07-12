package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.FrostbalanceHybridCommandBase;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GlobalPardonCommand extends FrostbalanceHybridCommandBase {

    public GlobalPardonCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "pardon"
        }, true);
    }

    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {

        String result = "";
        String targetId;
        User targetUser;

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
            result += "Unable to pardon '" + targetUser.getAsMention() + "'; they are not banned.";
        }

        Utilities.sendGuildMessage(event.getChannel(), result);

    }

    @Override
    public String publicInfo() {
        return "**" + bot.getPrefix() + "pardon PLAYER** - pardons a player across all servers.";
    }

    @Override
    public String privateInfo() {
        return null;
    }

}
