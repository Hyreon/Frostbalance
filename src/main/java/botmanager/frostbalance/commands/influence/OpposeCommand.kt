package botmanager.frostbalance.commands.influence

import botmanager.Utilities
import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.Influence
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.FrostbalanceGuildCommandBase
import botmanager.frostbalance.command.GuildCommandContext
import java.util.*

class OpposeCommand(bot: Frostbalance) : FrostbalanceGuildCommandBase(bot, arrayOf(
        "oppose",
        "o"
), AuthorityLevel.GENERIC) {

    override fun executeWithGuild(context: GuildCommandContext, vararg params: String) {
        val resultLines: MutableList<String> = ArrayList()
        var transferAmount = Influence(params[params.size - 1])
        val bMember = context.member
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
            resultLines.add("You don't have that much influence to use. You will instead use all of your influence.")
        } else if (transferAmount.isNegative || !transferAmount.isNonZero) { //'else' allows you to bluff when you have 0 influence.
            resultLines.add("You have to spend *some* influence to oppose someone.")
            context.sendResponse(resultLines)
            return
        }
        bMember.adjustInfluence(transferAmount.negate())
        if (targetMember == bMember) {
            resultLines.add("You lose " + transferAmount + " influence in " + context.jdaGuild.name + " as a result of hitting yourself.")
            context.sendResponse(resultLines)
            return
        }

        val refundAmount: Influence
        refundAmount = if (context.isPublic) {
            context.message.delete().queue()
            resultLines.add(bMember.effectiveName + " has *opposed* " + targetMember.effectiveName + ", reducing their influence here.")
            val reduceAmount = targetMember.adjustInfluence(transferAmount.negate())
            if (reduceAmount > 0) {
                resultLines.add("You have *opposed* " + targetMember.effectiveName + " silently, reducing their influence in " + context.guild.name + " by $reduceAmount.")
                Utilities.sendPrivateMessage(targetMember.userWrapper.user, String.format("%s has *opposed* you, reducing your influence in %s by %s.",
                        bMember.effectiveName,
                        context.guild.name,
                        reduceAmount))
            } else {
                context.sendPrivateResponse("Your bluff has had no effect. No other player has been notified.")
            }
            reduceAmount
        } else {
            if (transferAmount.applyModifier(PRIVATE_MODIFIER) > 0) {
                val reduceAmount = targetMember.adjustInfluence(transferAmount.applyModifier(PRIVATE_MODIFIER).negate())
                resultLines.add("You have *opposed* " + targetMember.effectiveName + " silently, reducing their influence in " + context.guild.name + " by $reduceAmount.")
                Utilities.sendPrivateMessage(targetMember.userWrapper.user, String.format("You have been smeared! Your influence in %s has been reduced by %s.",
                        context.guild.name,
                        reduceAmount))
                reduceAmount.reverseModifier(PRIVATE_MODIFIER)
                        .add(transferAmount.remainderOfModifier(PRIVATE_MODIFIER))
            } else {
                transferAmount
            }
        }
        if (refundAmount.isNonZero) {
            resultLines.add("You have been refunded $refundAmount that would have gone unused.")
            bMember.adjustInfluence(refundAmount)
        }
        context.sendResponse(resultLines)
        return
    }

    override fun info(authorityLevel: AuthorityLevel, isPublic: Boolean): String {
        return if (isPublic) {
            "**" + bot.prefix + "__o__ppose PLAYER AMOUNT** - Oppose another player, reducing your influence and theirs by the set amount"
        } else {
            "**" + bot.prefix + "__o__ppose PLAYER AMOUNT** - Oppose another player secretly (they don't know who you are), reducing your influence by the set amount, and theirs by 35% of that"
        }
    }

    companion object {
        private const val PRIVATE_MODIFIER = 0.35
    }
}