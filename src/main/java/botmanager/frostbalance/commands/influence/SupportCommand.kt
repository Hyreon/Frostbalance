package botmanager.frostbalance.commands.influence

import botmanager.Utilities
import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.Influence
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.FrostbalanceHybridCommandBase
import botmanager.frostbalance.command.CommandContext
import java.util.*

/**
 *
 * @author MC_2018 <mc2018.git></mc2018.git>@gmail.com>
 */
class SupportCommand(bot: Frostbalance) : FrostbalanceHybridCommandBase(bot, arrayOf(
        bot.prefix + "support",
        bot.prefix + "s"
), AuthorityLevel.GENERIC, Condition.GUILD_EXISTS) {
    override fun runHybrid(eventWrapper: CommandContext, vararg params: String) {
        val resultLines: MutableList<String> = ArrayList()
        var transferAmount = Influence(params[params.size - 1])
        val bMember = eventWrapper.botMember!!
        val targetName = java.lang.String.join(" ", *params.copyOfRange(0, params.size - 1))
        val targetUser = bot.getUserByName(targetName)
        if (!targetUser.isPresent) {
            resultLines.add("Could not find user '$targetName'.")
            eventWrapper.sendResponse(resultLines)
            return
        }
        val targetMember = targetUser.get().getMember(eventWrapper.guildId!!)
        if (transferAmount.greaterThan(bMember.influence)) {
            transferAmount = bMember.influence
            resultLines.add("You don't have that much influence to give. You will instead use all of your influence.")
        } else if (transferAmount.isNegative || !transferAmount.isNonZero) { //'else' allows you to bluff when you have 0 influence.
            resultLines.add("You have to spend *some* influence to support someone.")
            eventWrapper.sendResponse(resultLines)
            return
        }
        if (targetMember == bMember) {
            resultLines.add("You give yourself " + transferAmount + " influence in " + eventWrapper.guild!!.name + " because you are awesome.")
            eventWrapper.sendResponse(resultLines)
            return
        }
        bMember.adjustInfluence(transferAmount.negate())
        if (eventWrapper.isPublic) {
            eventWrapper.message.delete().queue()
            resultLines.add(bMember.effectiveName + " has *supported* " + targetMember.effectiveName + ", increasing their influence here.")
            Utilities.sendPrivateMessage(targetMember.userWrapper.user, String.format("%s has *supported* you, increasing your influence in %s by %s.",
                    bMember.effectiveName,
                    eventWrapper.botGuild!!.name,
                    transferAmount))
            targetMember.adjustInfluence(transferAmount)
        } else {
            resultLines.add("You have *supported* " + targetMember.effectiveName + " secretly, increasing their influence in " + eventWrapper.botGuild!!.name + ".")
            Utilities.sendPrivateMessage(targetMember.userWrapper.user, String.format("You have been supported secretly by " + bMember.effectiveName + ". Your influence in %s has been increased by %s.",
                    eventWrapper.botGuild!!.name,
                    transferAmount.applyModifier(PRIVATE_MODIFIER)))
            targetMember.adjustInfluence(transferAmount.applyModifier(PRIVATE_MODIFIER))
        }
        eventWrapper.sendResponse(resultLines)
        return
    }

    override fun info(authorityLevel: AuthorityLevel, isPublic: Boolean): String {
        return if (isPublic) {
            "**" + bot.prefix + "__s__upport PLAYER AMOUNT** - Support another player, giving them the set amount of influence"
        } else {
            "**" + bot.prefix + "__s__upport PLAYER AMOUNT** - Support another player privately (they know who you are), giving them 50% of what you give choose to give"
        }
    }

    companion object {
        private const val PRIVATE_MODIFIER = 0.5
    }
}