package botmanager.frostbalance.commands.influence;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.HotMap;
import botmanager.frostbalance.MemberWrapper;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommandBase;
import botmanager.frostbalance.command.GuildCommandContext;
import botmanager.frostbalance.menu.CheckMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class CheckCommand extends FrostbalanceGuildCommandBase {

    HotMap<TextChannel, Collection<Pair<User, User>>> privateCheckRequests = new HotMap<>();
    HotMap<TextChannel, Collection<CheckMenu>> checkMenuCache = new HotMap<>();

    public CheckCommand(Frostbalance bot) {
        super(bot, new String[] {
                "check"
        }, AuthorityLevel.GENERIC);
    }

    @Override
    protected void executeWithGuild(GuildCommandContext context, String... params) {
        if (context.isPublic()) runPublic(context, String.join(" ", params));
        else runPrivate(context, String.join(" ", params));
    }

    public void runPublic(GuildCommandContext context, String message) {

        String result;
        UserWrapper targetUser;

        if (message.isEmpty()) {
            result = info(context.getAuthority(), true);
            context.sendResponse(result);
            return;
        }

        targetUser = bot.getUserByName(message);

        if (targetUser == null) {
            result = "Couldn't find user '" + message + "'.";
            context.sendResponse(result);
            return;
        }

        CheckMenu menu = new CheckMenu(bot, context.getJDAGuild(), context.getJDAUser());
        menu.send(context.getChannel(), targetUser);
        addToCheckCache((TextChannel) context.getChannel(), menu);

        if (targetUser.memberIn(context.getGuild()).hasAuthority(AuthorityLevel.BOT)) {
            result = "Uh, sure?";
            context.sendResponse(result);
            menu.PERFORM_CHECK.applyReaction();
        }

    }

    public void runPrivate(GuildCommandContext context, String message) {

        String targetId;
        String result;
        User targetUser;
        boolean newRequest;

        Guild guild = context.getJDAGuild();

        if (message.isEmpty()) {
            result = info(context.getAuthority(), true);
            Utilities.sendPrivateMessage(context.getJDAUser(), result);
            return;
        }

        targetId = Utilities.findUserId(guild, message);

        if (targetId == null) {
            result = "Couldn't find user '" + message + "'.";
            Utilities.sendPrivateMessage(context.getJDAUser(), result);
            return;
        }

        targetUser = context.getJDA().getUserById(targetId);
        newRequest = addPrivateCheck(guild, context.getJDAUser(), context.getJDA().getUserById(targetId));

        if (targetUser.equals(bot.getJDA().getSelfUser())) {
            result = "Uh, sure?";
            Utilities.sendPrivateMessage(context.getJDAUser(), result);
            addPrivateCheck(guild, targetUser, context.getJDAUser());
            newRequest = false;
        }

        if (newRequest) {
            Utilities.sendPrivateMessage(targetUser, targetUser.getAsMention() + ", " + guild.getMember(context.getJDAUser()).getEffectiveName() + " would like to" +
                    " check if you have more influence than them. Send a check request to them to accept; ignore this to decline.");
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
            if (menu.getJdaActor().equals(menuToAdd.getJdaActor()) && menu.getChallenger().equals(menuToAdd.getChallenger())) {
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
        MemberWrapper firstMember = bot.getMemberWrapper(firstUser.getId(), guild.getId());
        MemberWrapper targetMember = bot.getMemberWrapper(targetUser.getId(), guild.getId());
        if (firstMember.getInfluence().compareTo(targetMember.getInfluence()) > 0) {
            Utilities.sendPrivateMessage(firstUser, firstMember.getEffectiveName() + " has **more** influence than " + targetMember.getEffectiveName() + ".");

            if (!targetUser.equals(bot.getJDA().getSelfUser())) {
                Utilities.sendPrivateMessage(targetUser, firstMember.getEffectiveName() + " has **more** influence than " + targetMember.getEffectiveName() + ".");
            }
            } else if (firstMember.getInfluence().equals(targetMember.getInfluence())) {
            if (firstMember.equals(targetMember)) {
                Utilities.sendPrivateMessage(firstUser, "To everyone's surprise, " + targetMember.getEffectiveName() + " has *as much* influence as " + firstMember.getEffectiveName() + ".");
            } else {
                Utilities.sendPrivateMessage(firstUser, targetMember.getEffectiveName() + " has *as much* influence as " + firstMember.getEffectiveName() + ".");
                if (!targetUser.equals(bot.getJDA().getSelfUser())) {
                    Utilities.sendPrivateMessage(targetUser, targetMember.getEffectiveName() + " has *as much* influence as " + firstMember.getEffectiveName() + ".");
                }
            }
        } else {
            Utilities.sendPrivateMessage(firstUser, targetMember.getEffectiveName() + " has **more** influence than " + firstMember.getEffectiveName() + ".");
            if (!targetUser.equals(bot.getJDA().getSelfUser())) {
                Utilities.sendPrivateMessage(targetUser, targetMember.getEffectiveName() + " has **more** influence than " + firstMember.getEffectiveName() + ".");
            }
        }
    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (isPublic) {
            return "**" + bot.getPrefix() + "check** PLAYER - sends a request to a player to see who has more influence in this guild; result is posted in this channel";
        } else {
            return "**" + bot.getPrefix() + "check** PLAYER - sends a request to a player to see who has more influence in a guild; result is posted privately to both players";
        }
    }
}
