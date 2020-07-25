package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceSplitCommandBase;
import botmanager.frostbalance.menu.BanManageMenu;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;

//TODO create a menu for global/guild bans/pardons
public class SystemBanCommand extends FrostbalanceSplitCommandBase {

    public SystemBanCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "ban"
        }, AuthorityLevel.GUILD_ADMIN);
    }

    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {
        String result = "";
        boolean found = false;
        String targetId;

        targetId = Utilities.findUserId(event.getGuild(), message);

        if (targetId == null) {
            targetId = Utilities.findBannedUserId(event.getGuild(), message);
        }

        if (targetId == null) {
            result += "Could not find user " + message + ".";
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        User targetUser = event.getJDA().getUserById(targetId);

        try {
            new BanManageMenu(bot, event.getGuild(), targetUser).send(event.getChannel(), event.getAuthor());
        } catch (HierarchyException e) {
            Utilities.sendGuildMessage(event.getChannel(), "You can't ban system admins with this command. Demote them first.");
        }

    }

    @Override
    public String publicInfo(AuthorityLevel authorityLevel) {
        if (authorityLevel.hasAuthority(AUTHORITY_LEVEL)) {
            return "**" + bot.getPrefix() + "ban PLAYER** - bans a player across all servers.";
        } else return null;
    }

    @Override
    public String privateInfo(AuthorityLevel authorityLevel) {
        return null;
    }

}
