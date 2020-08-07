package botmanager.frostbalance.commands.influence

import botmanager.Utilities
import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.Influence
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.FrostbalanceGuildCommandBase
import botmanager.frostbalance.command.GuildCommandContext
import java.util.*

/**
 *
 * @author MC_2018 <mc2018.git></mc2018.git>@gmail.com>
 */
class SupportCommand(bot: Frostbalance) : FrostbalanceGuildCommandBase(bot, arrayOf(
        "support",
        "s"
), AuthorityLevel.GENERIC) {

    override fun executeWithGuild(context: GuildCommandContext, vararg params: String) {
        val resultLines: MutableList<String> = ArrayList()
        var transferAmount = Influence(params[params.size - 1])
        val bMember = context.member!!
        val targetName = java.lang.String.join(" ", *params.copyOfRange(0, params.size - 1))
        val targetUser = bot.getUserByName(targetName)
        if (targetUser == null) {
            resultLines.add("Could not find user '$targetName'.")
            context.sendResponse(resultLines)
            return
        }
        val targetMember = targetUser.getMember(context.guild.id)
        if (transferAmount.greaterThan(bMember.influence)) {
            transferAmount = bMember.influence
            resultLines.add("You don't have that much influence to give. You will instead use all of your influence.")
        } else if (transferAmount.isNegative || !transferAmount.isNonZero) { //'else' allows you to bluff when you have 0 influence.
            resultLines.add("You have to spend *some* influence to support someone.")
            context.sendResponse(resultLines)
            return
        }
        if (targetMember == bMember) {
            resultLines.add("You give yourself " + transferAmount + " influence in " + context.jdaGuild!!.name + " because you are awesome.")
            context.sendResponse(resultLines)
            return
        }
        bMember.adjustInfluence(transferAmount.negate())
        if (context.isPublic) {
            context.message.delete().queue()
            resultLines.add(bMember.effectiveName + " has *supported* " + targetMember.effectiveName + ", increasing their influence here.")
            Utilities.sendPrivateMessage(targetMember.userWrapper.user, String.format("%s has *supported* you, increasing your influence in %s by %s.",
                    bMember.effectiveName,
                    context.guild!!.name,
                    transferAmount))
            targetMember.adjustInfluence(transferAmount)
        } else {

            if (transferAmount.applyModifier(PRIVATE_MODIFIER) > 0) {
                resultLines.add("You have *supported* " + targetMember.effectiveName + " secretly, increasing their influence in " + context.guild!!.name + " by ${transferAmount.applyModifier(PRIVATE_MODIFIER)}.")
                Utilities.sendPrivateMessage(targetMember.userWrapper.user, String.format("You have been supported secretly by " + bMember.effectiveName + ". Your influence in %s has been increased by %s.",
                        context.guild!!.name,
                        transferAmount.applyModifier(PRIVATE_MODIFIER)))
                targetMember.adjustInfluence(transferAmount.applyModifier(PRIVATE_MODIFIER))
            } else {
                resultLines.add("After rounding, your support would have no effect. Your influence has been refunded.")
                bMember.adjustInfluence(transferAmount)
            }
        }
        context.sendResponse(resultLines)
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