package botmanager.frostbalance.commands.admin;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.generic.GenericMessageReceivedEventWrapper;

public class AdjustCommand extends FrostbalanceHybridCommandBase {

    public AdjustCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "adjust"
        }, AuthorityLevel.GUILD_ADMIN);
    }

    @Override
    protected void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String message) {
        String[] words;
        String id;
        String name;
        String result;
        Influence amount;

        if (eventWrapper.getGuild() == null) {
            result = "You need to set a default guild to adjust influence.";
            eventWrapper.sendResponse(result);
            return;
        }

        words = message.split(" ");

        if (words.length < 2) {
            eventWrapper.sendResponse("Proper format: " + "**" + bot.getPrefix() + "adjust USER AMOUNT**");
            return;
        }

        try {
            amount = new Influence(words[words.length - 1]);
        } catch (NumberFormatException e) {
            eventWrapper.sendResponse("Proper format: " + "**" + bot.getPrefix() + "adjust USER AMOUNT**");
            return;
        }

        name = Utilities.combineArrayStopAtIndex(words, words.length - 1);
        id = Utilities.findUserId(eventWrapper.getGuild(), name);

        if (id == null) {
            eventWrapper.sendResponse("Couldn't find user '" + name + "'.");
            return;
        }

        bot.changeUserInfluence(eventWrapper.getGuild().getMemberById(id), amount);

        if (eventWrapper.isPublic()) {
            eventWrapper.getMessage().delete().complete();
        }

        eventWrapper.sendResponse("Your adjustment of "
                        + eventWrapper.getGuild().getMemberById(id).getEffectiveName()
                        + " has been noted, giving them " + amount + " influence.");

        if (amount.getThousandths() > 0) {
            Utilities.sendPrivateMessage(eventWrapper.getGuild().getMemberById(id).getUser(),
                    eventWrapper.getGuild().getMemberById(eventWrapper.getAuthor().getId()).getEffectiveName() + " has adjusted your influence, changing it by " + amount + " in " +
                            eventWrapper.getGuild().getName() + ".");
        }

    }

    @Override
    public String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (authorityLevel.hasAuthority(AUTHORITY_LEVEL))
            return "**" + bot.getPrefix() + "adjust USER AMOUNT** - changes influence of someone else (don't @ them) in this server";
        else return null;
    }
}

