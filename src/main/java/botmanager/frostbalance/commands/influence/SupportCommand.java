package botmanager.frostbalance.commands.influence;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.MemberWrapper;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.command.GenericMessageReceivedEventWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author MC_2018 <mc2018.git@gmail.com>
 */
public class SupportCommand extends FrostbalanceHybridCommandBase {

    private static final double PRIVATE_MODIFIER = 0.5;

    public SupportCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "support",
                bot.getPrefix() + "s"
        }, AuthorityLevel.GENERIC, Condition.GUILD_EXISTS);
    }

    @Override
    protected void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String... params) {
        List<String> resultLines = new ArrayList<>();

        Influence transferAmount = new Influence(params[params.length - 1]);
        MemberWrapper bMember = eventWrapper.getBotMember().get();

        String targetName = String.join(" ", Arrays.copyOfRange(params, 0, params.length - 1));
        Optional<UserWrapper> targetUser = bot.getUserByName(targetName);
        if (!targetUser.isPresent()) {
            resultLines.add("Could not find user '" + targetName + "'.");
            eventWrapper.sendResponse(resultLines);
            return;
        }
        MemberWrapper targetMember = targetUser.get().getMember(eventWrapper.getGuildId().get());

        if (transferAmount.greaterThan(bMember.getInfluence())) {
            transferAmount = bMember.getInfluence();
            resultLines.add("You don't have that much influence to give. You will instead use all of your influence.");
        } else if (transferAmount.isNegative() || !transferAmount.isNonZero()) { //'else' allows you to bluff when you have 0 influence.
            resultLines.add("You have to spend *some* influence to support someone.");
            eventWrapper.sendResponse(resultLines);
            return;
        }

        if (targetMember.equals(bMember)) {
            resultLines.add("You give yourself " + transferAmount + " influence in " + eventWrapper.getGuild().get().getName() + " because you are awesome.");
            eventWrapper.sendResponse(resultLines);
            return;
        }

        bMember.adjustInfluence(transferAmount.negate());

        if (eventWrapper.isPublic()) {
            eventWrapper.getMessage().delete().queue();
            resultLines.add(bMember.getEffectiveName() + " has *supported* " + targetMember.getEffectiveName() + ", increasing their influence here.");
            Utilities.sendPrivateMessage(targetMember.getUserWrapper().getUser().get(),
                    String.format("%s has *supported* you, increasing your influence in %s by %s.",
                            bMember.getEffectiveName(),
                            eventWrapper.getBotGuild().get().getName(),
                            transferAmount));
            targetMember.adjustInfluence(transferAmount);
        } else {
            resultLines.add("You have *supported* " + targetMember.getEffectiveName() + " secretly, increasing their influence in " + eventWrapper.getBotGuild().get().getName() + ".");
            Utilities.sendPrivateMessage(targetMember.getUserWrapper().getUser().get(),
                    String.format("You have been supported secretly by " + bMember.getEffectiveName() + ". Your influence in %s has been increased by %s.",
                            eventWrapper.getBotGuild().get().getName(),
                            transferAmount.applyModifier(PRIVATE_MODIFIER)));
            targetMember.adjustInfluence(transferAmount.applyModifier(PRIVATE_MODIFIER));
        }
        eventWrapper.sendResponse(resultLines);
        return;

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (isPublic) {
            return "**" + bot.getPrefix() + "__s__upport PLAYER AMOUNT** - Support another player, giving them the set amount of influence";
        } else {
            return "**" + bot.getPrefix() + "__s__upport PLAYER AMOUNT** - Support another player privately (they know who you are), giving them 50% of what you give choose to give";
        }
    }

}
