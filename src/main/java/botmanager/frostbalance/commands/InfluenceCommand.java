package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.generic.GenericMessageReceivedEventWrapper;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class InfluenceCommand extends FrostbalanceHybridCommandBase {

    public InfluenceCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "influence"
        }, AuthorityLevel.GENERIC);
    }

    @Override
    protected void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String message) {
        String id;
        String result, publicPost;
        String[] words;

        words = message.split(" ");

        id = eventWrapper.getAuthor().getId();

        if (eventWrapper.getGuild() == null || (words.length >= 1 && words[0].equalsIgnoreCase("all"))) {

            result = "Influence in all guilds:" + "\n";

            for (Guild guild : eventWrapper.getJDA().getGuilds()) {
                if (guild.getMember(eventWrapper.getAuthor()) == null) {
                    continue; //this user isn't in this guild.
                }
                result += "**" + guild.getName() + "**: " + String.format("%.3f", bot.getUserInfluence(guild, eventWrapper.getAuthor())) + "\n";
            }

            publicPost = "Your influence for all servers has been sent to you via PM.";

        } else if (message.length() > 0) {
            if (wouldAuthorize(eventWrapper.getGuild(), eventWrapper.getAuthor())) {
                id = Utilities.findUserId(eventWrapper.getGuild(), message);

                if (id == null) {
                    result = "Could not find user '" + message + "'.";
                    eventWrapper.sendResponse(result);
                    return;
                } else {
                    publicPost = "This user's influence has been sent to you via PM.";
                }
            } else {
                publicPost = "If you want to find the influence of a different player, you must ask them.\n"
                        + "Your influence has been sent to you via PM.";
            }
        } else {
            publicPost = "Your influence has been sent to you via PM.";
        }

        Member member = eventWrapper.getGuild().getMemberById(id);
        double influence = bot.getUserInfluence(member);

        if (influence <= 0) {
            result = "You have *no* influence in **" + eventWrapper.getGuild().getName() + "**.";
        } else {
            result = "You have **" + String.format("%.3f", influence) + "** influence in **" + eventWrapper.getGuild().getName() + "**.";
        }

        if (eventWrapper.isPublic()) {
            eventWrapper.sendResponse(publicPost);
        }
        eventWrapper.sendPrivateResponse(result);
    }

    @Override
    public String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (isPublic && authorityLevel.hasAuthority(AuthorityLevel.BOT_ADMIN)) {
            return ""
                    + "**" + bot.getPrefix() + "influence USER** - sends the influence of another user\n"
                    + "**" + bot.getPrefix() + "influence** - sends your influence on this server via PM";
        } else if (!isPublic && authorityLevel.hasAuthority(AuthorityLevel.BOT_ADMIN)) {
            return ""
                    + "**" + bot.getPrefix() + "influence USER** - sends the influence of another user\n"
                    + "**" + bot.getPrefix() + "influence** - sends your influence on your default server\n" +
                    "**" + bot.getPrefix() + "influence ALL** - sends your influence on all servers";
        } else if (isPublic) {
            return ""
                    + "**" + bot.getPrefix() + "influence** - sends your influence on this server via PM";
        } else {
            return ""
                    + "**" + bot.getPrefix() + "influence** - sends your influence on your default server\n" +
                    "**" + bot.getPrefix() + "influence ALL** - sends your influence on all servers";
        }
    }
}
