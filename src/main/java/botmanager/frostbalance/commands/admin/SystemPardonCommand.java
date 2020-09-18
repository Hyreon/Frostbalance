package botmanager.frostbalance.commands.admin;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.ContextLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommand;
import botmanager.frostbalance.command.GuildMessageContext;
import botmanager.frostbalance.menu.PardonManageMenu;

public class SystemPardonCommand extends FrostbalanceGuildCommand {

    public SystemPardonCommand(Frostbalance bot) {
        super(bot, new String[] {
                "syspardon"
        }, AuthorityLevel.GUILD_ADMIN, ContextLevel.PUBLIC_MESSAGE);
    }

    @Override
    protected void executeWithGuild(GuildMessageContext context, String... params) {

        String result = "";
        boolean found = false;
        String targetName = String.join(" ", params);
        UserWrapper targetUser = getBot().getUserByName(targetName, context.getGuild());

        if (targetUser == null) {
            result += "Could not find user " + targetName + ".";
            context.sendResponse(result);
            return;
        }

        new PardonManageMenu(getBot(), context, targetUser).send(context.getChannel(), context.getAuthor());

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + getBot().getPrefix() + "pardon PLAYER** - revokes a system ban on this player, local or global.";
    }

}
