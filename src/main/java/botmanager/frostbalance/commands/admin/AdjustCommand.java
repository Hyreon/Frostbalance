package botmanager.frostbalance.commands.admin;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.MemberWrapper;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.command.GenericMessageReceivedEventWrapper;

public class AdjustCommand extends FrostbalanceHybridCommandBase {

    public AdjustCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "adjust"
        }, AuthorityLevel.GUILD_ADMIN, Conditions.GUILD_EXISTS);
    }

    @Override
    protected void runHybrid(GenericMessageReceivedEventWrapper e, String[] params) {
        String id;
        String name;
        String result;
        Influence amount;

        if (params.length < 2) {
            result = "Proper format: " + "**" + bot.getPrefix() + "adjust USER AMOUNT**";
            e.sendResponse(result);
            return;
        }

        try {
            amount = new Influence(params[params.length - 1]);
        } catch (NumberFormatException ex) {
            e.sendResponse("Proper format: " + "**" + bot.getPrefix() + "adjust USER AMOUNT**");
            return;
        }

        name = Utilities.combineArrayStopAtIndex(params, params.length - 1);
        id = Utilities.findUserId(e.getGuild().get(), name);

        if (id == null) {
            e.sendResponse("Couldn't find user '" + name + "'.");
            return;
        }

        MemberWrapper recipient = bot.getMember(id, e.getGuildId().get());

        recipient.adjustInfluence(amount);

        if (e.isPublic()) {
            e.getMessage().delete().complete();
        }

        e.sendResponse("Your adjustment of "
                        + recipient.getEffectiveName()
                        + " has been noted, giving them " + amount + " influence.");

        if (amount.isNonZero()) {
            Utilities.sendPrivateMessage(recipient.getUserWrapper().getUser().get(),
                    e.getBotMember().get().getEffectiveName() + " has adjusted your influence, changing it by " + amount + " in " +
                            e.getBotGuild().get().getName() + ".");
        }

    }

    @Override
    public String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (authorityLevel.hasAuthority(AUTHORITY_LEVEL))
            return "**" + bot.getPrefix() + "adjust USER AMOUNT** - changes influence of someone else (don't @ them) in this server";
        else return null;
    }
}

