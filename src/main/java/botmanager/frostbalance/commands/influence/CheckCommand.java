package botmanager.frostbalance.commands.influence;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.HotMap;
import botmanager.frostbalance.MemberWrapper;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.ContextLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommand;
import botmanager.frostbalance.command.GuildMessageContext;
import botmanager.frostbalance.menu.CheckMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class CheckCommand extends FrostbalanceGuildCommand {

    HotMap<TextChannel, Collection<Pair<User, User>>> privateCheckRequests = new HotMap<>();
    HotMap<TextChannel, Collection<CheckMenu>> checkMenuCache = new HotMap<>();

    public CheckCommand(Frostbalance bot) {
        super(bot, new String[] {
                "check"
        }, AuthorityLevel.GENERIC, ContextLevel.ANY);
    }

    @Override
    protected void executeWithGuild(GuildMessageContext context, String... params) {
        if (context.isPublic()) runPublic(context, String.join(" ", params));
        else runPrivate(context, String.join(" ", params));
    }

    public void runPublic(GuildMessageContext context, String message) {

        String result;
        UserWrapper targetUser;

        if (message.isEmpty()) {
            result = info(context.getAuthority(), true);
            context.sendResponse(result);
            return;
        }

        targetUser = getBot().getUserByName(message, context.getGuild());

        if (targetUser == null) {
            result = "Couldn't find user '" + message + "'.";
            context.sendResponse(result);
            return;
        }

        CheckMenu menu = new CheckMenu(getBot(), context, context.getAuthor());
        menu.send(context.getChannel(), targetUser);
        addToCheckCache((TextChannel) context.getChannel(), menu);

        if (targetUser.memberIn(context.getGuild()).hasAuthority(AuthorityLevel.SELF)) {
            result = "Uh, sure?";
            context.sendResponse(result);
            menu.PERFORM_CHECK.applyReaction();
        }

    }

    public void runPrivate(GuildMessageContext context, String message) {

        String targetId;
        String result;
        User targetUser;
        boolean newRequest;

        Guild guild = context.getJDAGuild();

        if (message.isEmpty()) {
            result = info(context.getAuthority(), true);
            context.sendResponse(result);
            return;
        }

        targetId = Utilities.findUserId(guild, message);

        if (targetId == null) {
            result = "Couldn't find user '" + message + "'.";
            context.sendResponse(result);
            return;
        }

        targetUser = context.getJDA().getUserById(targetId);
        newRequest = addPrivateCheck(guild, context.getJdaUser(), context.getJDA().getUserById(targetId));

        if (targetUser.equals(getBot().getJDA().getSelfUser())) {
            result = "Uh, sure?";
            Utilities.sendPrivateMessage(context.getJdaUser(), result);
            addPrivateCheck(guild, targetUser, context.getJdaUser());
            newRequest = false;
        }

        if (newRequest) {
            Utilities.sendPrivateMessage(targetUser, context.buildEmbed(targetUser.getAsMention() + ", " + context.getMember().getEffectiveName() + " would like to" +
                    " check if you have more influence than them. Send a check request to them to accept; ignore this to decline.", false));
        }

    }

    private boolean addPrivateCheck(Guild guild, User firstUser, User targetUser) {
        Collection<Pair<User, User>> channelCheckRequests = privateCheckRequests.getOrDefault(guild, new HashSet<>());
        if (channelCheckRequests.contains(Pair.of(targetUser, firstUser))) {
            channelCheckRequests.remove(Pair.of(targetUser, firstUser));
            runPrivateCheck(guild, targetUser, firstUser);
            return false;
        } else {
            if (!channelCheckRequests.add(Pair.of(firstUser, targetUser))) {
                Utilities.sendPrivateMessage(firstUser, "You already have a check request with this user!");
                return false;
            }
            return true;
        }
    }

    /**
     * Adds an item to the checkMenu cache. If multiple menus with the same pair of players exist on a channel,
     * the older prompt is disabled.
     * @param channel
     * @param menuToAdd
     */
    private void addToCheckCache(TextChannel channel, CheckMenu menuToAdd) {
        Collection<CheckMenu> channelCheckRequests = checkMenuCache.getOrDefault(channel, new ArrayList<>());
        CheckMenu menuToRemove = null;
        for (CheckMenu menu : channelCheckRequests) {
            if (menu.getActor().equals(menuToAdd.getActor()) && menu.getChallenger().equals(menuToAdd.getChallenger())) {
                menuToRemove = menu;
                break;
            }
        }
        if (menuToRemove != null) {
            if (!menuToRemove.isClosed()) {
                menuToRemove.EXPIRE_CHECK.reactEvent();
            }
            channelCheckRequests.remove(menuToRemove);
        }
        channelCheckRequests.add(menuToAdd);
    }

    private void runPrivateCheck(Guild guild, User firstUser, User targetUser) {
        MemberWrapper firstMember = getBot().getMemberWrapper(firstUser.getId(), guild.getId());
        MemberWrapper targetMember = getBot().getMemberWrapper(targetUser.getId(), guild.getId());
        if (firstMember.getInfluence().compareTo(targetMember.getInfluence()) > 0) {
            Utilities.sendPrivateMessage(firstUser, firstMember.getEffectiveName() + " has **more** influence than " + targetMember.getEffectiveName() + ".");

            if (!targetUser.equals(getBot().getJDA().getSelfUser())) {
                Utilities.sendPrivateMessage(targetUser, firstMember.getEffectiveName() + " has **more** influence than " + targetMember.getEffectiveName() + ".");
            }
            } else if (firstMember.getInfluence().equals(targetMember.getInfluence())) {
            if (firstMember.equals(targetMember)) {
                Utilities.sendPrivateMessage(firstUser, "To everyone's surprise, " + targetMember.getEffectiveName() + " has *as much* influence as " + firstMember.getEffectiveName() + ".");
            } else {
                Utilities.sendPrivateMessage(firstUser, targetMember.getEffectiveName() + " has *as much* influence as " + firstMember.getEffectiveName() + ".");
                if (!targetUser.equals(getBot().getJDA().getSelfUser())) {
                    Utilities.sendPrivateMessage(targetUser, targetMember.getEffectiveName() + " has *as much* influence as " + firstMember.getEffectiveName() + ".");
                }
            }
        } else {
            Utilities.sendPrivateMessage(firstUser, targetMember.getEffectiveName() + " has **more** influence than " + firstMember.getEffectiveName() + ".");
            if (!targetUser.equals(getBot().getJDA().getSelfUser())) {
                Utilities.sendPrivateMessage(targetUser, targetMember.getEffectiveName() + " has **more** influence than " + firstMember.getEffectiveName() + ".");
            }
        }
    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (isPublic) {
            return "**" + getBot().getPrefix() + "check** PLAYER - sends a request to a player to see who has more influence in this guild; result is posted in this channel";
        } else {
            return "**" + getBot().getPrefix() + "check** PLAYER - sends a request to a player to see who has more influence in a guild; result is posted privately to both players";
        }
    }
}
