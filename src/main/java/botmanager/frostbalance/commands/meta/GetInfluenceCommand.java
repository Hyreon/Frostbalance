package botmanager.frostbalance.commands.meta;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.MemberWrapper;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.command.CommandContext;
import net.dv8tion.jda.api.entities.Guild;

public class GetInfluenceCommand extends FrostbalanceHybridCommandBase {

    public GetInfluenceCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "influence"
        }, AuthorityLevel.GENERIC);
    }

    @Override
    protected void runHybrid(CommandContext eventWrapper, String... params) {
        String id;
        String result, publicPost;

        id = eventWrapper.getAuthor().getId();

        if (eventWrapper.getGuild() == null || (params.length >= 1 && params[0].equalsIgnoreCase("all"))) {

            result = "Influence in all guilds:" + "\n";

            for (Guild guild : eventWrapper.getJDA().getGuilds()) {

                MemberWrapper bMember = eventWrapper.getBotUser().getMember(guild.getId());

                result += "**" + guild.getName() + "**: " + bMember.getInfluence();

                Influence remaining = bMember.getInfluenceSource().getInfluenceLeft();
                if (remaining.isNonZero()) {
                    result += " (**+" + String.format("%s", remaining) + "** from unclaimed daily)";
                }

                result += "\n";
            }

            publicPost = "Your influence for all servers has been sent to you via PM.";

        } else {

            if (params.length > 0) {
                if (eventWrapper.getAuthority().hasAuthority(AuthorityLevel.GUILD_ADMIN)) {
                    id = Utilities.findUserId(eventWrapper.getGuild(), String.join(" ", params));

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

            MemberWrapper bMember = bot.getUserWrapper(id).getMember(eventWrapper.getGuildId());
            Influence influence = bMember.getInfluence();
            Influence remaining = bMember.getInfluenceSource().getInfluenceLeft();

            if (bMember.equals(eventWrapper.getBotMember())) {
                if (influence.getValue() <= 0) {
                    result = "You have *no* influence in **" + eventWrapper.getBotGuild().getName() + "**.";
                } else {
                    result = "You have **" + influence + "** influence in **" + eventWrapper.getBotGuild().getName() + "**.";
                }
            } else {
                if (influence.getValue() <= 0) {
                    result = bMember.getEffectiveName() + " has *no* influence in **" + eventWrapper.getBotGuild().getName() + "**.";
                } else {
                    result = bMember.getEffectiveName() + " has **" + influence + "** influence in **" + eventWrapper.getBotGuild().getName() + "**.";
                }
            }

            if (remaining.isNonZero()) {
                result += " (**+" + remaining + "** from unclaimed daily)";
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
