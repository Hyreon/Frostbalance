package botmanager.frostbalance.commands.influence;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.MemberWrapper;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.ContextLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommand;
import botmanager.frostbalance.command.GuildMessageContext;
import botmanager.frostbalance.menu.input.ConfirmationMenu;
import net.dv8tion.jda.api.entities.Member;

public class InaugurateCommand extends FrostbalanceGuildCommand {

    public InaugurateCommand(Frostbalance bot) {
        super(bot, new String[] {
                "inaugurate",
                "transfer"
        }, AuthorityLevel.NATION_LEADER, ContextLevel.PUBLIC_MESSAGE);
    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (isPublic) return null;
        return ""
                + "**" + getBot().getPrefix() + "inaugurate USER** - makes someone else server owner.";
    }

    @Override
    protected void executeWithGuild(GuildMessageContext context, String... params) {
        String result;

        String targetName = String.join(" ", params);
        Member authorAsMember = context.getJDAMember();

        if (!authorAsMember.equals(context.getGuild().getLeaderAsMember())) {
            result = "You have to be the owner to transfer ownership of the server peaceably.";
            context.sendResponse(result);
            return;
        }

        UserWrapper targetUser = getBot().getUserByName(targetName, context.getGuild());

        if (targetUser == null) {
            result = "Couldn't find user '" + targetName + "'.";
            context.sendResponse(result);
            return;
        }

        MemberWrapper targetMember = targetUser.memberIn(context.getGuild());

        if (!targetMember.getOnline()) {
            result = targetName + " isn't in " + context.getGuild().getName() + " right now.";
            context.sendResponse(result);
            return;
        }

        if (targetMember.hasAuthority(AuthorityLevel.GUILD_ADMIN)) {
            if (targetMember.hasAuthority(AuthorityLevel.SELF)) {
                result = "A very generous offer, but I can't accept.";
            } else {
                result = "Staff members are prohibited from getting server ownership through transfer.";
            }
            context.sendResponse(result);
            return;
        }

        new ConfirmationMenu(getBot(), context, () -> {

            context.getGuild().inaugurate(targetMember.getJdaMember());

            context.sendResponse("**" + authorAsMember.getEffectiveName() + "** has transferred ownership to " +
                    targetMember.getJdaMember().getAsMention() + " for this server.");

        }, "This will remove all of your abilities as leader, and grant those abilities to " + targetMember.getJdaMember().getAsMention() + ".\nAre you sure?");

    }
}
