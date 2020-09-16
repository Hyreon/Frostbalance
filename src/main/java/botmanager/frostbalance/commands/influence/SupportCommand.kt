package botmanager.frostbalance.commands.influence

import botmanager.Utilities
import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*
import botmanager.frostbalance.menu.ConfirmationMenu
import java.lang.NumberFormatException
import java.util.*

class SupportCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf(
        "support",
        "s"
), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {
        val arguments = ArgumentStream(params)

        val resultLines: MutableList<String> = ArrayList()
        val bMember = context.member!!
        val targetName = arguments.exhaust(1)
        val targetMember = bot.getUserByName(targetName, context.guild)?.memberIfIn(context.guild)
        if (targetMember == null) {
            resultLines.add("Could not find user '$targetName'.")
            context.sendMultiLineResponse(resultLines)
            return
        }
        var transferAmount = try {
            arguments.nextInfluence() ?: return context.sendResponse("No influence found!")
        } catch (e: NumberFormatException) {
            return context.sendResponse("That last bit wasn't a number. Try again.")
        }
        if (transferAmount.greaterThan(bMember.influence)) {
            transferAmount = bMember.influence
            resultLines.add("You don't have that much influence to give. You will instead use all of your influence.")
        } else if (transferAmount.isNegative || !transferAmount.nonZero) { //'else' allows you to bluff when you have 0 influence.
            resultLines.add("You have to spend *some* influence to support someone.")
            context.sendMultiLineResponse(resultLines)
            return
        }
        if (targetMember == bMember) {
            resultLines.add("You support yourself with " + transferAmount + " influence in ${context.guild.name}. You should return the favor.")
            context.sendMultiLineResponse(resultLines)
            return
        }
        bMember.adjustInfluence(transferAmount.negate())
        if (context.isPublic) {
            context.message.delete().queue()
            resultLines.add(bMember.effectiveName + " has *supported* " + targetMember.effectiveName + ", increasing their influence here.")
            Utilities.sendPrivateMessage(targetMember.userWrapper.jdaUser, context.buildEmbed(String.format("%s has *supported* you, increasing your influence in %s by %s.",
                    bMember.effectiveName,
                    context.guild!!.name,
                    transferAmount), false))
            targetMember.adjustInfluence(transferAmount)
            context.sendMultiLineResponse(resultLines)
        } else {
            ConfirmationMenu(bot, context, {
                if (transferAmount.applyModifier(PRIVATE_MODIFIER) > 0) {
                    resultLines.add("You have *supported* " + targetMember.effectiveName + " secretly, increasing their influence in " + context.guild.name + " by ${transferAmount.applyModifier(PRIVATE_MODIFIER)}.")
                    Utilities.sendPrivateMessage(targetMember.userWrapper.jdaUser, context.buildEmbed(String.format("You have been supported secretly by " + bMember.effectiveName + ". Your influence in %s has been increased by %s.",
                            context.guild.name,
                            transferAmount.applyModifier(PRIVATE_MODIFIER)), false))
                    targetMember.adjustInfluence(transferAmount.applyModifier(PRIVATE_MODIFIER))

                    val refundAmount = transferAmount.remainderOfModifier(PRIVATE_MODIFIER)
                    if (refundAmount.nonZero) {
                        resultLines.add("You have been refunded $refundAmount that would have gone unused.")
                        bMember.adjustInfluence(transferAmount)
                    }
                } else {
                    resultLines.add("After rounding, your support would have no effect. Your influence has been refunded.")
                    bMember.adjustInfluence(transferAmount)
                }
                context.sendMultiLineResponse(resultLines)
            }, "Are you sure you want to support ${targetMember.effectiveName} privately? Only ${PRIVATE_MODIFIER*100}% of your influence will be used.")
                    .send(context.channel, context.author)
        }
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