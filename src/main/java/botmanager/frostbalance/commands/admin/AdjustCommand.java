package botmanager.frostbalance.commands.admin;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.MemberWrapper;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.ContextLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommand;
import botmanager.frostbalance.command.GuildMessageContext;

public class AdjustCommand extends FrostbalanceGuildCommand {

    public AdjustCommand(Frostbalance bot) {
        super(bot, new String[] {
                "adjust"
        }, AuthorityLevel.GUILD_ADMIN, ContextLevel.ANY);
    }

    @Override
    protected void executeWithGuild(GuildMessageContext context, String[] params) {
        String name;
        String result;
        Influence amount;

        if (params.length < 2) {
            result = "Proper format: " + "**" + getBot().getPrefix() + "adjust USER AMOUNT**";
            context.sendResponse(result);
            return;
        }

        try {
            amount = new Influence(params[params.length - 1]);
        } catch (NumberFormatException ex) {
            context.sendResponse("Proper format: " + "**" + getBot().getPrefix() + "adjust USER AMOUNT**");
            return;
        }

        name = Utilities.combineArrayStopAtIndex(params, params.length - 1);
        UserWrapper bUser = getBot().getUserByName(name);

        if (bUser == null) {
            context.sendResponse("Couldn't find user '" + name + "'.");
            return;
        }

        MemberWrapper recipient = bUser.memberIn(context.getGuild());

        recipient.adjustInfluence(amount);

        if (context.isPublic()) {
            context.getMessage().delete().complete();
        }

        context.sendResponse("Your adjustment of "
                        + recipient.getEffectiveName()
                        + " has been noted, giving them " + amount + " influence.");

        if (amount.isNonZero()) {
            Utilities.sendPrivateMessage(recipient.getUserWrapper().getJdaUser(),
                    context.buildEmbed( context.getMember().getEffectiveName() + " has adjusted your influence, changing it by " + amount + " in " +
                            context.getGuild().getName() + ".", false));
        }

    }

    @Override
    public String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + getBot().getPrefix() + "adjust USER AMOUNT** - changes influence of someone else (don't @ them) in this server";
    }
}

