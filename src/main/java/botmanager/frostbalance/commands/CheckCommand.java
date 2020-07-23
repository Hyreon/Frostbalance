package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.HotMap;
import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceSplitCommandBase;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.Collection;
import java.util.HashSet;

public class CheckCommand extends FrostbalanceSplitCommandBase {

    HotMap<TextChannel, Collection<Pair<User, User>>> checkRequests = new HotMap<>();

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
        boolean newRequest;

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
        newRequest = addCheck(event.getChannel(), event.getAuthor(), event.getJDA().getUserById(targetId));

        if (bot.getAuthority(event.getGuild().getMemberById(targetId)).hasAuthority(AuthorityLevel.BOT)) {
            result = "Uh, sure?";
            Utilities.sendGuildMessage(event.getChannel(), result);
            newRequest = addCheck(event.getChannel(), targetUser, event.getAuthor());
        }

        if (newRequest) {
            Utilities.sendGuildMessage(event.getChannel(), targetUser.getAsMention() + ", " + event.getGuild().getMember(event.getAuthor()).getEffectiveName() + " would like to" +
                    " check if you have more influence than them. Send a check request to them to accept; ignore this to decline.");
        }

    }

    private boolean addCheck(TextChannel channel, User firstUser, User targetUser) {
        Collection<Pair<User, User>> channelCheckRequests = checkRequests.getOrDefault(channel, new HashSet<>());
        if (channelCheckRequests.contains(Pair.of(targetUser, firstUser))) {
            channelCheckRequests.remove(Pair.of(targetUser, firstUser));
            check(channel, targetUser, firstUser);
            return false;
        } else {
            if (!channelCheckRequests.add(Pair.of(firstUser, targetUser))) {
                Utilities.sendGuildMessage(channel, "You already have a check request with this user!");
                return false;
            }
            return true;
        }
    }

    private void check(TextChannel channel, User firstUser, User targetUser) {
        Member firstMember = channel.getGuild().getMember(firstUser);
        Member targetMember = channel.getGuild().getMember(targetUser);
        if (bot.getUserInfluence(firstMember) > bot.getUserInfluence(targetMember)) {
            Utilities.sendGuildMessage(channel, firstMember.getEffectiveName() + " has **more** influence than " + targetMember.getEffectiveName() + ".");
        } else if (bot.getUserInfluence(firstMember) == bot.getUserInfluence(targetMember)) {
            if (firstMember.equals(targetMember)) {
                Utilities.sendGuildMessage(channel, "To everyone's surprise, " + targetMember.getEffectiveName() + " has *as much* influence as " + firstMember.getEffectiveName() + ".");
            } else {
                Utilities.sendGuildMessage(channel, targetMember.getEffectiveName() + " has *as much* influence as " + firstMember.getEffectiveName() + ".");
            }
        } else {
            Utilities.sendGuildMessage(channel, targetMember.getEffectiveName() + " has **more** influence than " + firstMember.getEffectiveName() + ".");
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
