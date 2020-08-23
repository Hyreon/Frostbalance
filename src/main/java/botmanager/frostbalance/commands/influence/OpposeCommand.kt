package botmanager.frostbalance.commands.influence

import botmanager.Utilities
import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.Influence
import botmanager.frostbalance.command.*
import botmanager.frostbalance.menu.ArgumentObligatorMenu
import java.util.*

class OpposeCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf(
        "oppose",
        "o"
), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun executeWithGuild(context: GuildCommandContext, vararg params: String) {
        val arguments = ArgumentStream(params)

        val resultLines: MutableList<String> = ArrayList()
        val bMember = context.member
        val targetName = arguments.exhaustArguments(1)
        val targetUser = bot.getUserByName(targetName)
        if (targetUser == null) {
            resultLines.add("Could not find user '$targetName'.")
            return context.sendEmbedResponse(resultLines)
        }
        var transferAmount = try {
            arguments.nextInfluence() ?: return context.sendResponse("No influence found!")
        } catch (e: NumberFormatException) {
            return context.sendResponse("That last bit wasn't a number. Try again.")
        }
        val targetMember = targetUser.memberIn(context.guild.id)
        if (transferAmount.greaterThan(bMember.influence)) {
            transferAmount = bMember.influence
            resultLines.add("You don't have that much influence to use. You will instead use all of your influence.")
        } else if (transferAmount.isNegative || !transferAmount.isNonZero) { //'else' allows you to bluff when you have 0 influence.
            resultLines.add("You have to spend *some* influence to oppose someone.")
            return context.sendEmbedResponse(resultLines)
        }

        bMember.adjustInfluence(transferAmount.negate())
        if (targetMember == bMember) {
            resultLines.add("You lose " + transferAmount + " influence in ${context.guild.name} as a result of hitting yourself.")
            return context.sendEmbedResponse(resultLines)
        }

        val refundAmount: Influence
        refundAmount = if (context.isPublic) {
            context.message.delete().queue()
            val reduceAmount = transferAmount.subtract(targetMember.adjustInfluence(transferAmount.negate()))
            when {
                reduceAmount > 0 -> {
                    resultLines.add(bMember.effectiveName + " has *opposed* " + targetMember.effectiveName + ", reducing their influence here.")
                    Utilities.sendPrivateMessage(targetMember.userWrapper.jdaUser, String.format("%s has *opposed* you, reducing your influence in %s by %s.",
                            bMember.effectiveName,
                            context.guild.name,
                            reduceAmount))
                }
                transferAmount.isNonZero -> {
                    context.sendPrivateResponse("The target player is out of influence. Nothing has happened.")
                }
                else -> {
                    context.sendPrivateResponse("Your bluff has had no effect. No other player has been notified.")
                }
            }
            transferAmount.subtract(reduceAmount)
        } else {
            if (transferAmount.applyModifier(PRIVATE_MODIFIER) > 0) {
                val remainAmount = targetMember.adjustInfluence(transferAmount.applyModifier(PRIVATE_MODIFIER).negate())
                val reduceAmount = transferAmount.applyModifier(PRIVATE_MODIFIER).subtract(remainAmount)
                when {
                    reduceAmount > 0 -> {
                        resultLines.add("You have *opposed* " + targetMember.effectiveName + " silently, reducing their influence in " + context.guild.name + " by $reduceAmount.")
                        Utilities.sendPrivateMessage(targetMember.userWrapper.jdaUser, String.format("You have been smeared! Your influence in %s has been reduced by %s.",
                                context.guild.name,
                                reduceAmount))
                    }
                    transferAmount.isNonZero -> {
                        context.sendPrivateResponse("The target player is out of influence. Nothing has happened.")
                    }
                    else -> {
                        context.sendPrivateResponse("Your bluff has had no effect. No other player has been notified.")
                    }
                }

                remainAmount.reverseModifier(PRIVATE_MODIFIER)
                        .add(transferAmount.remainderOfModifier(PRIVATE_MODIFIER))
            } else {
                transferAmount
            }
        }
        if (refundAmount.isNonZero) {
            resultLines.add("You have been refunded $refundAmount that would have gone unused.")
            bMember.adjustInfluence(refundAmount)
        }
        return context.sendEmbedResponse(resultLines)
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