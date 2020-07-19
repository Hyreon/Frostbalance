package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceSplitCommandBase;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class GlobalPardonCommand extends FrostbalanceSplitCommandBase {

    public GlobalPardonCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "pardon"
        }, AuthorityLevel.BOT_ADMIN);
    }

    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {

        String result = "";
        User targetUser;

        targetUser = Utilities.findBannedUser(event.getGuild(), message);

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
    public String publicInfo(AuthorityLevel authorityLevel) {
        if (authorityLevel.hasAuthority(AUTHORITY_LEVEL)) {
            return "**" + bot.getPrefix() + "pardon PLAYER** - pardons a player across all servers.";
        } else {
            return null;
        }
    }

    @Override
    public String privateInfo(AuthorityLevel authorityLevel) {
        return null;
    }

}
