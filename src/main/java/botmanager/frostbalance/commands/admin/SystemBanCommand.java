package botmanager.frostbalance.commands.admin;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.ContextLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommand;
import botmanager.frostbalance.command.GuildMessageContext;
import botmanager.frostbalance.menu.BanManageMenu;
import net.dv8tion.jda.api.exceptions.HierarchyException;

public class SystemBanCommand extends FrostbalanceGuildCommand {

    public SystemBanCommand(Frostbalance bot) {
        super(bot, new String[] {
                "sysban"
        }, AuthorityLevel.GUILD_ADMIN, ContextLevel.PUBLIC_MESSAGE);
    }

    @Override
    protected void executeWithGuild(GuildMessageContext context, String... params) {

        String result = "";
        boolean found = false;
        String targetName = String.join(" ", params);
        UserWrapper targetUser = getBot().getUserByName(targetName);

        if (targetUser == null) {
            result += "Could not find user " + targetName + ".";
            context.sendResponse(result);
            return;
        }

        try {
            new BanManageMenu(getBot(), context, targetUser).send(context.getChannel(), context.getAuthor());
        } catch (HierarchyException e) {
            context.sendResponse("You can't ban system admins with this command. Demote them first.");
        }

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + getBot().getPrefix() + "ban PLAYER** - creates a system ban on this player, local or global.";
    }
}
