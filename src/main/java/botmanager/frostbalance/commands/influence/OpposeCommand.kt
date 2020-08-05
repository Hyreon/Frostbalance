package botmanager.frostbalance.commands.influence

import botmanager.Utilities
import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.Influence
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.FrostbalanceHybridCommandBase
import botmanager.frostbalance.command.CommandContext
import java.util.*

class OpposeCommand(bot: Frostbalance) : FrostbalanceHybridCommandBase(bot, arrayOf(
        bot.prefix + "oppose",
        bot.prefix + "o"
), AuthorityLevel.GENERIC, Condition.GUILD_EXISTS) {
    override fun runHybrid(eventWrapper: CommandContext, vararg params: String) {
        val resultLines: MutableList<String> = ArrayList()
        var transferAmount = Influence(params[params.size - 1])
        val bMember = eventWrapper.botMember!!
        val targetName = java.lang.String.join(" ", *params.copyOfRange(0, params.size - 1))
        val targetUser = bot.getUserByName(targetName)
        if (targetUser == null) {
            resultLines.add("Could not find user '$targetName'.")
            eventWrapper.sendResponse(resultLines)
            return
        }
        val targetMember = targetUser.getMember(eventWrapper.guildId!!)
        if (transferAmount.greaterThan(bMember.influence)) {
            transferAmount = bMember.influence
            resultLines.add("You don't have that much influence to use. You will instead use all of your influence.")
        } else if (transferAmount.isNegative || !transferAmount.isNonZero) { //'else' allows you to bluff when you have 0 influence.
            resultLines.add("You have to spend *some* influence to oppose someone.")
            eventWrapper.sendResponse(resultLines)
            return
        }
        bMember.adjustInfluence(transferAmount.negate())
        if (targetMember == bMember) {
            resultLines.add("You lose " + transferAmount + " influence in " + eventWrapper.guild!!.name + " as a result of hitting yourself.")
            eventWrapper.sendResponse(resultLines)
            return
        }
        if (eventWrapper.isPublic) {
            eventWrapper.message.delete().queue()
            resultLines.add(bMember.effectiveName + " has *opposed* " + targetMember.effectiveName + ", reducing their influence here.")
            Utilities.sendPrivateMessage(targetMember.userWrapper.user, String.format("%s has *opposed* you, reducing your influence in %s by %s.",
                    bMember.effectiveName,
                    eventWrapper.botGuild!!.name,
                    transferAmount))
            targetMember.adjustInfluence(transferAmount.negate())
        } else {
            resultLines.add("You have *opposed* " + targetMember.effectiveName + " silently, reducing their influence in " + eventWrapper.botGuild!!.name + ".")
            Utilities.sendPrivateMessage(targetMember.userWrapper.user, String.format("You have been smeared anonymously! Your influence in %s has been reduced by %s.",
                    eventWrapper.botGuild!!.name,
                    transferAmount.applyModifier(PRIVATE_MODIFIER)))
            targetMember.adjustInfluence(transferAmount.applyModifier(PRIVATE_MODIFIER).negate())
        }
        eventWrapper.sendResponse(resultLines)
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