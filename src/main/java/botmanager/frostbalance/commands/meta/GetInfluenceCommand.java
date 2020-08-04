package botmanager.frostbalance.commands.meta;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.DailyInfluenceSource;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.command.GenericMessageReceivedEventWrapper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class GetInfluenceCommand extends FrostbalanceHybridCommandBase {

    public GetInfluenceCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "influence"
        }, AuthorityLevel.GENERIC);
    }

    @Override
    protected void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String... params) {
        String id;
        String result, publicPost;

        id = eventWrapper.getAuthor().getId();

        if (!eventWrapper.getGuild().isPresent() || (params.length >= 1 && params[0].equalsIgnoreCase("all"))) {

            result = "Influence in all guilds:" + "\n";

            for (Guild guild : eventWrapper.getJDA().getGuilds()) {
                if (guild.getMember(eventWrapper.getAuthor()) == null) {
                    continue; //this user isn't in this guild.
                }

                result += "**" + guild.getName() + "**: " + String.format("%s", bot.getUserInfluence(guild, eventWrapper.getAuthor()));

                Influence remaining = DailyInfluenceSource.DAILY_INFLUENCE_CAP.subtract(bot.getUserDailyAmount(guild, eventWrapper.getAuthor()));
                if (remaining.getValue() > 0) {
                    result += " (**+" + String.format("%s", remaining) + "** from unclaimed daily)";
                }

                result += "\n";
            }

            publicPost = "Your influence for all servers has been sent to you via PM.";

        } else {

            if (params.length > 0) {
                if (eventWrapper.getAuthority().hasAuthority(AuthorityLevel.GUILD_ADMIN)) {
                    id = Utilities.findUserId(eventWrapper.getGuild().get(), String.join(" ", params));

                    if (id == null) {
                        result = "Could not find user '" + String.join(" ", params) + "'.";
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

            Member member = eventWrapper.getGuild().get().getMemberById(id);
            Influence influence = bot.getUserInfluence(member);
            Influence remaining = DailyInfluenceSource.DAILY_INFLUENCE_CAP.subtract(bot.getUserDailyAmount(member));

            if (member.equals(eventWrapper.getMember())) {
                if (influence.getValue() <= 0) {
                    result = "You have *no* influence in **" + eventWrapper.getGuild().get().getName() + "**.";
                } else {
                    result = "You have **" + String.format("%s", influence) + "** influence in **" + eventWrapper.getGuild().get().getName() + "**.";
                }
            } else {
                if (influence.getValue() <= 0) {
                    result = member.getEffectiveName() + " has *no* influence in **" + eventWrapper.getGuild().get().getName() + "**.";
                } else {
                    result = member.getEffectiveName() + " has **" + String.format("%s", influence) + "** influence in **" + eventWrapper.getGuild().get().getName() + "**.";
                }
            }

            if (remaining.getValue() > 0) {
                result += " (**+" + String.format("%s", remaining) + "** from unclaimed daily)";
            }

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
