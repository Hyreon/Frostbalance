package botmanager.frostbalance.commands.admin;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.GuildWrapper;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommandBase;
import botmanager.frostbalance.command.GuildCommandContext;
import botmanager.frostbalance.menu.ConfirmationMenu;
import net.dv8tion.jda.api.entities.Member;

public class InterveneCommand extends FrostbalanceGuildCommandBase {

    public InterveneCommand(Frostbalance bot) {
        super(bot, new String[] {
                "reset"
        }, AuthorityLevel.GUILD_ADMIN);
    }

    @Override
    public void executeWithGuild(GuildCommandContext context, String[] params) {

        String id;
        String result;
        Member currentOwner;

        GuildWrapper bGuild = context.getGuild();

        id = context.getJDAUser().getId();

        currentOwner = bGuild.getLeaderAsMember();

        if (currentOwner == null) {
            result = "You can't reset if there's no owner.";
            context.sendResponse(result);
            return;
        }

        new ConfirmationMenu(bot, () -> {
            bGuild.reset();

            String result1;
            result1 = currentOwner.getAsMention() + " has been removed as leader by administrative action.";
            result1 += "\nAll non-system bans and player roles have been reset.";

            context.sendResponse(result1);
        },
                "This will depose the current leader, unban all players, and delete all roles.\nAre you sure?")
                .send(context.getChannel(), context.getJDAUser());



    }


    @Override
    public String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (!isPublic) return null;
        return ""
                + "**" + bot.getPrefix() + "reset** - Remove the current owner, unban all players, and reset all non-system roles. " +
                "The current owner can't become owner until a new owner is in place.";
    }

}
