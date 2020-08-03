package botmanager.frostbalance.commands.influence;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.HotMap;
import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceSplitCommandBase;
import botmanager.frostbalance.generic.GenericMessageReceivedEventWrapper;
import botmanager.frostbalance.menu.CheckMenu;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class CheckCommand extends FrostbalanceSplitCommandBase {

    HotMap<TextChannel, Collection<Pair<User, User>>> privateCheckRequests = new HotMap<>();
    HotMap<TextChannel, Collection<CheckMenu>> checkMenuCache = new HotMap<>();

    public CheckCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "check"
        }, AuthorityLevel.GENERIC);
    }

    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {

        String targetId;
        String result;
        User targetUser;

        if (message.isEmpty()) {
            result = info(bot.getAuthority(event.getGuild(), event.getAuthor()), true);
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        targetId = Utilities.findUserId(event.getGuild(), message);

        if (targetId == null) {
            result = "Couldn't find user '" + message + "'.";
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        targetUser = event.getJDA().getUserById(targetId);

        CheckMenu menu = new CheckMenu(bot, event.getGuild(), event.getAuthor());
        menu.send(event.getChannel(), targetUser);
        addToCheckCache(event.getChannel(), menu);

        if (bot.getAuthority(event.getGuild().getMemberById(targetId)).hasAuthority(AuthorityLevel.BOT)) {
            result = "Uh, sure?";
            Utilities.sendGuildMessage(event.getChannel(), result);
            menu.PERFORM_CHECK.applyReaction();
        }

    }

    @Override
    public void runPrivate(PrivateMessageReceivedEvent event, String message) {

        String targetId;
        String result;
        User targetUser;
        boolean newRequest;

        Guild guild = new GenericMessageReceivedEventWrapper(bot, event).getGuild();

        if (guild == null) {
            Utilities.sendPrivateMessage(event.getAuthor(), "You need to have a default guild to run this command.");
            return;
        }

        if (message.isEmpty()) {
            result = info(bot.getAuthority(guild, event.getAuthor()), true);
            Utilities.sendPrivateMessage(event.getAuthor(), result);
            return;
        }

        targetId = Utilities.findUserId(guild, message);

        if (targetId == null) {
            result = "Couldn't find user '" + message + "'.";
            Utilities.sendPrivateMessage(event.getAuthor(), result);
            return;
        }

        targetUser = event.getJDA().getUserById(targetId);
        newRequest = addPrivateCheck(guild, event.getAuthor(), event.getJDA().getUserById(targetId));

        if (targetUser.equals(bot.getJDA().getSelfUser())) {
            result = "Uh, sure?";
            Utilities.sendPrivateMessage(event.getAuthor(), result);
            addPrivateCheck(guild, targetUser, event.getAuthor());
            newRequest = false;
        }

        if (newRequest) {
            Utilities.sendPrivateMessage(targetUser, targetUser.getAsMention() + ", " + guild.getMember(event.getAuthor()).getEffectiveName() + " would like to" +
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
        Member firstMember = guild.getMember(firstUser);
        Member targetMember = guild.getMember(targetUser);
        if (bot.getUserInfluence(firstMember).compareTo(bot.getUserInfluence(targetMember)) > 0) {
            Utilities.sendPrivateMessage(firstUser, firstMember.getEffectiveName() + " has **more** influence than " + targetMember.getEffectiveName() + ".");

            if (!targetUser.equals(bot.getJDA().getSelfUser())) {
                Utilities.sendPrivateMessage(targetUser, firstMember.getEffectiveName() + " has **more** influence than " + targetMember.getEffectiveName() + ".");
            }
            } else if (bot.getUserInfluence(firstMember) == bot.getUserInfluence(targetMember)) {
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
    public String publicInfo(AuthorityLevel authorityLevel) {
        return "**" + bot.getPrefix() + "check** PLAYER - sends a request to a player to see who has more influence; must be done in the same channel";
    }

    @Override
    public String privateInfo(AuthorityLevel authorityLevel) {
        return null;
    }
}
