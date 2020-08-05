package botmanager.frostbalance.commands.influence;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.MemberWrapper;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.command.GenericMessageReceivedEventWrapper;

import java.util.Optional;

public class CoupCommand extends FrostbalanceHybridCommandBase {

    final String[] KEYWORDS = {
            bot.getPrefix() + "coup"
    };

    public CoupCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "coup"
        }, AuthorityLevel.GENERIC, Condition.PUBLIC);
    }

    @Override
    protected void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String... params) {

        String result, privateResult;
        boolean success;

        MemberWrapper bMember = eventWrapper.getBotMember().get();
        String guildId = eventWrapper.getGuildId().get();
        Optional<String> currentOwnerId = eventWrapper.getBotGuild().get().getOwnerId();

        if (bMember.hasBeenForciblyRemoved()) {
            result = "You have been recently removed by administrative action. Wait until someone else is leader.";
            eventWrapper.sendResponse(result);
            return;
        }

        if (!currentOwnerId.isPresent() ||
                !bot.getMemberWrapper(currentOwnerId.get(), guildId).getMember().isPresent()) {
            result = "**" + bMember.getEffectiveName() + "** is the first player to declare themselves leader, " +
                    "and is now leader!";
            privateResult = null;
            success = true;
        } else {

            MemberWrapper currentOwner = bot.getMemberWrapper(currentOwnerId.get(), guildId);

            if (currentOwner.equals(bMember)) {
                result = "You realize that you're no match for yourself, and call it off.";
                eventWrapper.sendResponse(result);
                return;
            }

            Influence influence = bMember.getInfluence();
            Influence ownerInfluence = currentOwner.getInfluence();

            if (influence.compareTo(ownerInfluence) > 0) {
                bMember.adjustInfluence(ownerInfluence.negate());
                currentOwner.adjustInfluence(ownerInfluence.negate());
                result = "**" + bMember.getEffectiveName() + "** has successfully supplanted **" +
                        currentOwner.getMember().get().getAsMention() + "** as leader, reducing both users' influence and becoming" +
                        " the new leader!";
                privateResult = "*This maneuver has cost you " + String.format("%s", ownerInfluence) + " influence. " +
                        currentOwner.getEffectiveName() + " has lost **ALL** of their influence.*";
                success = true;
            } else {
                bMember.adjustInfluence(influence.negate());
                currentOwner.adjustInfluence(influence.negate());
                result = "**" + bMember.getEffectiveName() + "** has attempted a coup on **" +
                        currentOwner.getMember().get().getAsMention() + "**, which has backfired. Both players have lost influence" +
                        " and the leader has not changed.";
                privateResult = "*This maneuver has cost you **ALL** of your influence. " +
                        currentOwner.getEffectiveName() + " has lost " + String.format("%s", influence) + " of their influence.*";
                success = false;
            }

        }

        if (success) {
            bot.getGuild(guildId).doCoup(eventWrapper.getAuthor());
        }

        eventWrapper.sendResponse(result);

        if (privateResult != null) {
            eventWrapper.sendPrivateResponse(privateResult);
        }

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (isPublic) {
            return ""
                    + "**" + bot.getPrefix() + "coup** - become server owner; this will drain both your influence and the influence " +
                    "of the current owner until one (or both) of you run out. For ties, the existing owner is still owner.";
        } else return null;
    }
}
