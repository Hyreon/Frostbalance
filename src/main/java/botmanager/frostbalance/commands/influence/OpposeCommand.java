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

public class OpposeCommand extends FrostbalanceHybridCommandBase {


    private static final double PRIVATE_MODIFIER = 0.35;

    public OpposeCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "oppose",
                bot.getPrefix() + "o"
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
            resultLines.add("You don't have that much influence to use. You will instead use all of your influence.");
        } else if (transferAmount.isNegative() || !transferAmount.isNonZero()) { //'else' allows you to bluff when you have 0 influence.
            resultLines.add("You have to spend *some* influence to oppose someone.");
            eventWrapper.sendResponse(resultLines);
            return;
        }

        bMember.adjustInfluence(transferAmount.negate());

        if (targetMember.equals(bMember)) {
            resultLines.add("You lose " + transferAmount + " influence in " + eventWrapper.getGuild().get().getName() + " as a result of hitting yourself.");
            eventWrapper.sendResponse(resultLines);
            return;
        }

        if (eventWrapper.isPublic()) {
            eventWrapper.getMessage().delete().queue();
            resultLines.add(bMember.getEffectiveName() + " has *opposed* " + targetMember.getEffectiveName() + ", reducing their influence here.");
            Utilities.sendPrivateMessage(targetMember.getUserWrapper().getUser().get(),
                    String.format("%s has *opposed* you, reducing your influence in %s by %s.",
                            bMember.getEffectiveName(),
                            eventWrapper.getBotGuild().get().getName(),
                            transferAmount));
            targetMember.adjustInfluence(transferAmount.negate());
        } else {
            resultLines.add("You have *opposed* " + targetMember.getEffectiveName() + " silently, reducing their influence in " + eventWrapper.getBotGuild().get().getName() + ".");
            Utilities.sendPrivateMessage(targetMember.getUserWrapper().getUser().get(),
                    String.format("You have been smeared anonymously! Your influence in %s has been reduced by %s.",
                            eventWrapper.getBotGuild().get().getName(),
                            transferAmount.applyModifier(PRIVATE_MODIFIER)));
            targetMember.adjustInfluence(transferAmount.applyModifier(PRIVATE_MODIFIER).negate());
        }
        eventWrapper.sendResponse(resultLines);
        return;

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (isPublic) {
            return "**" + bot.getPrefix() + "__o__ppose PLAYER AMOUNT** - Oppose another player, reducing your influence and theirs by the set amount";
        } else {
            return "**" + bot.getPrefix() + "__o__ppose PLAYER AMOUNT** - Oppose another player secretly (they don't know who you are), reducing your influence by the set amount, and theirs by 35% of that";
        }
    }
}
