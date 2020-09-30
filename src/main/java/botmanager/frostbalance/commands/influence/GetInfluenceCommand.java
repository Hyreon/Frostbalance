package botmanager.frostbalance.commands.influence;

import botmanager.frostbalance.*;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.ContextLevel;
import botmanager.frostbalance.command.FrostbalanceCommand;
import botmanager.frostbalance.command.MessageContext;
import org.jetbrains.annotations.NotNull;

public class GetInfluenceCommand extends FrostbalanceCommand {

    public GetInfluenceCommand(Frostbalance bot) {
        super(bot, new String[] {
                "influence"
        }, AuthorityLevel.GENERIC, ContextLevel.ANY);
    }

    @Override
    protected void execute(MessageContext context, String... params) {
        StringBuilder result;
        String publicPost;

        UserWrapper bUser = context.getAuthor();

        if (context.getGuild() == null || (params.length >= 1 && params[0].equalsIgnoreCase("all"))) {

            result = new StringBuilder("Influence in all guilds:" + "\n");

            for (GameNetwork gameNetwork : getBot().getNetworkList()) {

                if (gameNetwork.getAssociatedGuilds().isEmpty()) continue;
                if (context.getAuthor().playerIfIn(gameNetwork) == null) continue;

                result.append("__").append(gameNetwork.getId()).append("__\n");

                for (GuildWrapper guild : gameNetwork.getAssociatedGuilds()) {

                    MemberWrapper bMember = context.getAuthor().memberIfIn(guild);
                    if (bMember == null) continue;

                    result.append("**").append(guild.getName()).append("**: ").append(bMember.getInfluence());

                    Influence remaining = bMember.getInfluenceSource().getInfluenceLeft();
                    if (remaining.getNonZero()) {
                        if (guild.allows(context.getAuthor().playerIn(guild.getGameNetwork()))) {
                            result.append(" (**+").append(String.format("%s", remaining)).append("** from unclaimed daily)");
                        } else {
                            result.append(" (:passport_control: cannot gain influence)");
                        }
                    }

                    result.append("\n");

                }

            }

            publicPost = "Your influence for all servers has been sent to you via PM.";

        } else {

            if (params.length > 0) {
                if (context.getAuthority().hasAuthority(AuthorityLevel.GUILD_ADMIN)) {
                    bUser = getBot().getUserByName(String.join(" ", params), context.getGuild());

                    if (bUser == null) {
                        result = new StringBuilder("Could not find user '" + String.join(" ", params) + "'.");
                        context.sendResponse(result.toString());
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

            if (bUser.equals(context.getAuthor())) {
                if (influence.getValue() <= 0) {
                    result = new StringBuilder("You have *no* influence in **" + context.getGuild().getName() + "**.");
                } else {
                    result = new StringBuilder("You have **" + influence + "** influence in **" + context.getGuild().getName() + "**.");
                }
            } else {
                if (influence.getValue() <= 0) {
                    result = new StringBuilder(bMember.getEffectiveName() + " has *no* influence in **" + context.getGuild().getName() + "**.");
                } else {
                    result = new StringBuilder(bMember.getEffectiveName() + " has **" + influence + "** influence in **" + context.getGuild().getName() + "**.");
                }
            }

            if (remaining.getNonZero()) {
                if (context.getGuild().allows(context.getAuthor().playerIn(context.getGameNetwork()))) {
                    result.append(" (**+").append(String.format("%s", remaining)).append("** from unclaimed daily)");
                } else {
                    result.append(" (:passport_control: cannot gain influence)");
                }
            }

        }

        if (context.isPublic()) {
            context.sendResponse(publicPost);
        }
        context.sendPrivateResponse(result.toString());
    }

    @Override
    public String info(@NotNull AuthorityLevel authorityLevel, boolean isPublic) {
        if (authorityLevel.hasAuthority(AuthorityLevel.BOT_ADMIN)) {
            return ""
                    + "**" + getBot().getPrefix() + "influence USER** - sends the influence of another user\n"
                    + "**" + getBot().getPrefix() + "influence** - sends your influence on your default server\n" +
                    "**" + getBot().getPrefix() + "influence ALL** - sends your influence on all servers";
        } else {
            return ""
                    + "**" + getBot().getPrefix() + "influence** - sends your influence on your default server\n" +
                    "**" + getBot().getPrefix() + "influence ALL** - sends your influence on all servers";
        }
    }
}
