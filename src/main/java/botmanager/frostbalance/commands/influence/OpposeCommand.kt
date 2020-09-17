package botmanager.frostbalance.commands.influence

import botmanager.Utilities
import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.Influence
import botmanager.frostbalance.command.*
import botmanager.frostbalance.menu.input.ConfirmationMenu
import java.util.*

class OpposeCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf(
        "oppose",
        "o"
), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {
        val arguments = ArgumentStream(params)

        val resultLines: MutableList<String> = ArrayList()
        val bMember = context.member
        val targetName = arguments.exhaust(1)
        val targetMember = bot.getUserByName(targetName, context.guild)?.memberIfIn(context.guild)
        if (targetMember == null) {
            resultLines.add("Could not find member '$targetName'.")
            return context.sendMultiLineResponse(resultLines)
        }
        var transferAmount = try {
            arguments.nextInfluence() ?: return context.sendResponse("No influence found!")
        } catch (e: NumberFormatException) {
            return context.sendResponse("That last bit wasn't a number. Try again.")
        }
        if (transferAmount.greaterThan(bMember.influence)) {
            transferAmount = bMember.influence
            resultLines.add("You don't have that much influence to use. You will instead use all of your influence.")
        } else if (transferAmount.isNegative || !transferAmount.nonZero) { //'else' allows you to bluff when you have 0 influence.
            resultLines.add("You have to spend *some* influence to oppose someone.")
            return context.sendMultiLineResponse(resultLines)
        }

        bMember.adjustInfluence(transferAmount.negate())
        if (targetMember == bMember) {
            resultLines.add("You lose " + transferAmount + " influence in ${context.guild.name} as a result of hitting yourself.")
            return context.sendMultiLineResponse(resultLines)
        }

        var refundAmount: Influence
        if (context.isPublic) {
            context.message.delete().queue()
            val reduceAmount = transferAmount.subtract(targetMember.adjustInfluence(transferAmount.negate()))
            when {
                reduceAmount > 0 -> {
                    resultLines.add(bMember.effectiveName + " has *opposed* " + targetMember.effectiveName + ", reducing their influence here.")
                    Utilities.sendPrivateMessage(targetMember.userWrapper.jdaUser, context.buildEmbed(String.format("%s has *opposed* you, reducing your influence in %s by %s.",
                            bMember.effectiveName,
                            context.guild.name,
                            reduceAmount), false))
                }
                transferAmount.nonZero -> {
                    context.sendPrivateResponse("The target player is out of influence. Nothing has happened.")
                }
                else -> {
                    context.sendPrivateResponse("Your bluff has had no effect. No other player has been notified.")
                }
            }
            refundAmount = transferAmount.subtract(reduceAmount)
            if (refundAmount.nonZero) {
                resultLines.add("You have been refunded $refundAmount that would have gone unused.")
                bMember.adjustInfluence(refundAmount)
            }
            return context.sendMultiLineResponse(resultLines)
        } else {
            ConfirmationMenu(bot, context, {
                if (transferAmount.applyModifier(PRIVATE_MODIFIER) > 0) {
                    val remainAmount = targetMember.adjustInfluence(transferAmount.applyModifier(PRIVATE_MODIFIER).negate())
                    val reduceAmount = transferAmount.applyModifier(PRIVATE_MODIFIER).subtract(remainAmount)
                    when {
                        reduceAmount > 0 -> {
                            resultLines.add("You have *opposed* " + targetMember.effectiveName + " silently, reducing their influence in " + context.guild.name + " by $reduceAmount.")
                            Utilities.sendPrivateMessage(targetMember.userWrapper.jdaUser, context.buildEmbed(String.format("You have been smeared! Your influence in %s has been reduced by %s.",
                                    context.guild.name,
                                    reduceAmount), false))
                        }
                        transferAmount.nonZero -> {
                            context.sendPrivateResponse("The target player is out of influence. Nothing has happened.")
                        }
                        else -> {
                            context.sendPrivateResponse("Your bluff has had no effect. No other player has been notified.")
                        }
                    }

                    refundAmount = remainAmount.reverseModifier(PRIVATE_MODIFIER)
                            .add(transferAmount.remainderOfModifier(PRIVATE_MODIFIER))
                } else {
                    refundAmount = transferAmount
                }

                if (refundAmount.nonZero) {
                    resultLines.add("You have been refunded $refundAmount that would have gone unused.")
                    bMember.adjustInfluence(refundAmount)
                }
                context.sendMultiLineResponse(resultLines)
            }, "Are you sure you want to oppose ${targetMember.effectiveName} privately? Only ${PRIVATE_MODIFIER * 100}% of your influence will be used.")
                    .send(context.channel, context.author)
        }
    }

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
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