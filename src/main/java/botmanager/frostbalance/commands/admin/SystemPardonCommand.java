package botmanager.frostbalance.commands.admin;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommandBase;
import botmanager.frostbalance.command.GuildCommandContext;
import botmanager.frostbalance.menu.PardonManageMenu;

public class SystemPardonCommand extends FrostbalanceGuildCommandBase {

    public SystemPardonCommand(Frostbalance bot) {
        super(bot, new String[] {
                "pardon"
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

        new PardonManageMenu(bot, context, targetUser).send(context.getChannel(), context.getAuthor());

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + bot.getPrefix() + "pardon PLAYER** - revokes a system ban on this player, local or global.";
    }

}
