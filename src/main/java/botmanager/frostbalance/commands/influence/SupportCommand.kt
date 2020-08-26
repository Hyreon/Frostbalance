package botmanager.frostbalance.commands.influence

import botmanager.Utilities
import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.Influence
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.ContextLevel
import botmanager.frostbalance.command.FrostbalanceGuildCommand
import botmanager.frostbalance.command.GuildCommandContext
import java.util.*

//FIXME `.support Shade` returns an error with no error message.
class SupportCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf(
        "support",
        "s"
), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun executeWithGuild(context: GuildCommandContext, vararg params: String) {
        val resultLines: MutableList<String> = ArrayList()
        var transferAmount = Influence(params[params.size - 1])
        val bMember = context.member!!
        val targetName = java.lang.String.join(" ", *params.copyOfRange(0, params.size - 1))
        val targetUser = bot.getUserByName(targetName)
        if (targetUser == null) {
            resultLines.add("Could not find user '$targetName'.")
            context.sendEmbedResponse(resultLines)
            return
        }
        val targetMember = targetUser.memberIn(context.guild.id)
        if (transferAmount.greaterThan(bMember.influence)) {
            transferAmount = bMember.influence
            resultLines.add("You don't have that much influence to give. You will instead use all of your influence.")
        } else if (transferAmount.isNegative || !transferAmount.isNonZero) { //'else' allows you to bluff when you have 0 influence.
            resultLines.add("You have to spend *some* influence to support someone.")
            context.sendEmbedResponse(resultLines)
            return
        }
        if (targetMember == bMember) {
            resultLines.add("You support yourself with " + transferAmount + " influence in ${context.guild.name}. You should return the favor.")
            context.sendEmbedResponse(resultLines)
            return
        }
        bMember.adjustInfluence(transferAmount.negate())
        if (context.isPublic) {
            context.message.delete().queue()
            resultLines.add(bMember.effectiveName + " has *supported* " + targetMember.effectiveName + ", increasing their influence here.")
            Utilities.sendPrivateMessage(targetMember.userWrapper.jdaUser, context.buildEmbed(String.format("%s has *supported* you, increasing your influence in %s by %s.",
                    bMember.effectiveName,
                    context.guild!!.name,
                    transferAmount)))
            targetMember.adjustInfluence(transferAmount)
        } else {

            if (transferAmount.applyModifier(PRIVATE_MODIFIER) > 0) {
                resultLines.add("You have *supported* " + targetMember.effectiveName + " secretly, increasing their influence in " + context.guild.name + " by ${transferAmount.applyModifier(PRIVATE_MODIFIER)}.")
                Utilities.sendPrivateMessage(targetMember.userWrapper.jdaUser, context.buildEmbed(String.format("You have been supported secretly by " + bMember.effectiveName + ". Your influence in %s has been increased by %s.",
                        context.guild!!.name,
                        transferAmount.applyModifier(PRIVATE_MODIFIER))))
                targetMember.adjustInfluence(transferAmount.applyModifier(PRIVATE_MODIFIER))

                val refundAmount = transferAmount.remainderOfModifier(PRIVATE_MODIFIER)
                if (refundAmount.isNonZero) {
                    resultLines.add("You have been refunded $refundAmount that would have gone unused.")
                    bMember.adjustInfluence(transferAmount)
                }
            } else {
                resultLines.add("After rounding, your support would have no effect. Your influence has been refunded.")
                bMember.adjustInfluence(transferAmount)
            }
        }
        context.sendEmbedResponse(resultLines)
        return
    }

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
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