package botmanager.frostbalance.commands.influence;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.MemberWrapper;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommandBase;
import botmanager.frostbalance.command.GuildCommandContext;
import botmanager.frostbalance.menu.ConfirmationMenu;
import net.dv8tion.jda.api.entities.Member;

public class InaugurateCommand extends FrostbalanceGuildCommandBase {

    public InaugurateCommand(Frostbalance bot) {
        super(bot, new String[] {
                "inaugurate",
                "transfer"
        }, AuthorityLevel.SERVER_LEADER, Condition.PUBLIC);
    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (isPublic) return null;
        return ""
                + "**" + bot.getPrefix() + "inaugurate USER** - makes someone else server owner.";
    }

    @Override
    protected void executeWithGuild(GuildCommandContext context, String... params) {
        String result;

        String targetName = String.join(" ", params);
        Member authorAsMember = context.getJDAMember();

        if (!authorAsMember.equals(context.getGuild().getLeaderAsMember())) {
            result = "You have to be the owner to transfer ownership of the server peaceably.";
            context.sendResponse(result);
            return;
        }

        UserWrapper targetUser = bot.getUserByName(targetName);

        if (targetUser == null) {
            result = "Couldn't find user '" + targetName + "'.";
            context.sendResponse(result);
            return;
        }

        MemberWrapper targetMember = targetUser.getMember(context.getGuild());

        if (!targetMember.getOnline()) {
            result = targetName + " isn't in " + context.getGuild().getName() + " right now.";
            context.sendResponse(result);
            return;
        }

        if (targetMember.hasAuthority(AuthorityLevel.GUILD_ADMIN)) {
            if (targetMember.hasAuthority(AuthorityLevel.BOT)) {
                result = "A very generous offer, but I can't accept.";
            } else {
                result = "Staff members are prohibited from getting server ownership through transfer.";
            }
            context.sendResponse(result);
            return;
        }

        new ConfirmationMenu(bot, () -> {

            context.getGuild().inaugurate(targetMember.getMember());

            context.sendResponse("**" + authorAsMember.getEffectiveName() + "** has transferred ownership to " +
                    targetMember.getMember().getAsMention() + " for this server.");

        }, "This will remove all of your abilities as leader, and grant those abilities to " + targetMember.getMember().getAsMention() + ".\nAre you sure?");

    }
}
