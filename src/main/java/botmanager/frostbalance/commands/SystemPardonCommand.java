package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceSplitCommandBase;
import botmanager.frostbalance.menu.PardonManageMenu;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class SystemPardonCommand extends FrostbalanceSplitCommandBase {

    public SystemPardonCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "pardon"
        }, AuthorityLevel.GUILD_ADMIN);
    }

    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {

        String result = "";
        User targetUser;
        String targetId;

        targetId = Utilities.findBannedUserId(event.getGuild(), message);

        if (targetId == null) {
            targetId = Utilities.findUserId(event.getGuild(), message);
        }

        if (targetId == null) {
            result += "Could not find user " + message + ".";
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        targetUser = bot.getJDA().getUserById(targetId);

        new PardonManageMenu(bot, event.getGuild(), targetUser).send(event.getChannel(), event.getAuthor());

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
