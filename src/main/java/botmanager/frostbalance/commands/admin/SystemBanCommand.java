package botmanager.frostbalance.commands.admin;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommandBase;
import botmanager.frostbalance.command.GuildCommandContext;
import botmanager.frostbalance.menu.BanManageMenu;
import net.dv8tion.jda.api.exceptions.HierarchyException;

public class SystemBanCommand extends FrostbalanceGuildCommandBase {

    public SystemBanCommand(Frostbalance bot) {
        super(bot, new String[] {
                "ban"
        }, AuthorityLevel.GUILD_ADMIN);
    }

    @Override
    protected void executeWithGuild(GuildCommandContext context, String... params) {

        String result = "";
        boolean found = false;
        String targetName = String.join(" ", params);
        UserWrapper targetUser = bot.getUserByName(targetName);

        if (targetUser == null) {
            result += "Could not find user " + targetName + ".";
            context.sendResponse(result);
            return;
        }

        try {
            new BanManageMenu(bot, context.getJDAGuild(), targetUser.getUser()).send(context.getChannel(), context.getJDAUser());
        } catch (HierarchyException e) {
            context.sendResponse("You can't ban system admins with this command. Demote them first.");
        }

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + bot.getPrefix() + "ban PLAYER** - creates a system ban on this player, local or global.";
    }
}
