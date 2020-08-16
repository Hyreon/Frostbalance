package botmanager.frostbalance.commands.meta;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.MemberWrapper;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommandBase;
import botmanager.frostbalance.command.GuildCommandContext;
import net.dv8tion.jda.api.entities.Guild;

public class GetInfluenceCommand extends FrostbalanceGuildCommandBase {

    public GetInfluenceCommand(Frostbalance bot) {
        super(bot, new String[] {
                "influence"
        }, AuthorityLevel.GENERIC);
    }

    @Override
    protected void executeWithGuild(GuildCommandContext context, String... params) {
        String result, publicPost;

        UserWrapper bUser = context.getAuthor();

        if (context.getJDAGuild() == null || (params.length >= 1 && params[0].equalsIgnoreCase("all"))) {

            result = "Influence in all guilds:" + "\n";

            for (Guild guild : context.getJDA().getGuilds()) {

                MemberWrapper bMember = context.getAuthor().memberIn(guild.getId());

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
                if (context.getAuthority().hasAuthority(AuthorityLevel.GUILD_ADMIN)) {
                    bUser = bot.getUserByName(String.join(" ", params));

                    if (bUser == null) {
                        result = "Could not find user '" + String.join(" ", params) + "'.";
                        context.sendResponse(result);
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

            MemberWrapper bMember = bUser.memberIn(context.getGuild());
            Influence influence = bMember.getInfluence();
            Influence remaining = bMember.getInfluenceSource().getInfluenceLeft();

            if (bMember.equals(context.getMember())) {
                if (influence.getValue() <= 0) {
                    result = "You have *no* influence in **" + context.getGuild().getName() + "**.";
                } else {
                    result = "You have **" + influence + "** influence in **" + context.getGuild().getName() + "**.";
                }
            } else {
                if (influence.getValue() <= 0) {
                    result = bMember.getEffectiveName() + " has *no* influence in **" + context.getGuild().getName() + "**.";
                } else {
                    result = bMember.getEffectiveName() + " has **" + influence + "** influence in **" + context.getGuild().getName() + "**.";
                }
            }

            if (remaining.isNonZero()) {
                result += " (**+" + remaining + "** from unclaimed daily)";
            }

        }

        if (context.isPublic()) {
            context.sendResponse(publicPost);
        }
        context.sendPrivateResponse(result);
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
