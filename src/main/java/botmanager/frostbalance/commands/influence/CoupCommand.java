package botmanager.frostbalance.commands.influence;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceSplitCommandBase;
import botmanager.frostbalance.history.TerminationCondition;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class CoupCommand extends FrostbalanceSplitCommandBase {

    final String[] KEYWORDS = {
            bot.getPrefix() + "coup"
    };

    public CoupCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "coup"
        });
    }

    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {

        String id = event.getAuthor().getId();
        String result, privateResult;
        Member currentOwner;
        boolean found = false, success;

        currentOwner = bot.getOwner(event.getGuild());

        Member member = event.getGuild().getMemberById(id);

        if (!bot.hasBeenForciblyRemoved(member)) {
            result = "You have been recently removed by administrative action. Wait until someone else is leader.";
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        if (currentOwner == null) {
            result = "**" + event.getMember().getEffectiveName() + "** is the first player to declare themselves leader, " +
                    "and is now leader!";
            privateResult = null;
            success = true;
        } else {

            if (currentOwner.equals(member)) {
                result = "You realize that you're no match for yourself, and call it off.";
                Utilities.sendGuildMessage(event.getChannel(), result);
                return;
            }

            Influence influence = bot.getUserInfluence(member);
            Influence ownerInfluence = bot.getUserInfluence(currentOwner);

            if (influence.compareTo(ownerInfluence) > 0) {
                bot.changeUserInfluence(member, ownerInfluence.negate());
                bot.changeUserInfluence(currentOwner, ownerInfluence.negate());
                result = "**" + event.getMember().getEffectiveName() + "** has successfully supplanted **" +
                        currentOwner.getAsMention() + "** as leader, reducing both users' influence and becoming" +
                        " the new leader!";
                privateResult = "*This maneuver has cost you " + String.format("%s", ownerInfluence) + " influence. " +
                        currentOwner.getEffectiveName() + " has lost **ALL** of their influence.*";
                success = true;
            } else {
                bot.changeUserInfluence(member, influence.negate());
                bot.changeUserInfluence(currentOwner, influence.negate());
                result = "**" + event.getMember().getEffectiveName() + "** has attempted a coup on **" +
                        currentOwner.getAsMention() + "**, which has backfired. Both players have lost influence" +
                        " and the leader has not changed.";
                privateResult = "*This maneuver has cost you **ALL** of your influence. " +
                        currentOwner.getEffectiveName() + " has lost " + String.format("%s", influence) + " of their influence.*";
                success = false;
            }

        }

        if (success) {
            bot.endRegime(event.getGuild(), TerminationCondition.COUP);
            bot.startRegime(event.getGuild(), member.getUser());
        }

        Utilities.sendGuildMessage(event.getChannel(), result);

        if (privateResult != null) {
            Utilities.sendPrivateMessage(event.getAuthor(), privateResult);
        }

    }

    @Override
    public String publicInfo(AuthorityLevel authorityLevel) {
        return ""
                + "**" + bot.getPrefix() + "coup** - become server owner; this will drain both your influence and the influence " +
                "of the current owner until one (or both) of you run out. For ties, the existing owner is still owner.";
    }

    @Override
    public String privateInfo(AuthorityLevel authorityLevel) {
        return null;
    }

}
