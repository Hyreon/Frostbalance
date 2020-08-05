package botmanager.frostbalance.commands.influence

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.FrostbalanceHybridCommandBase
import botmanager.frostbalance.command.CommandContext

class CoupCommand(bot: Frostbalance) : FrostbalanceHybridCommandBase(bot, arrayOf(
        bot.prefix + "coup"
), AuthorityLevel.GENERIC, Condition.PUBLIC) {

    override fun runHybrid(eventWrapper: CommandContext, vararg params: String) {
        val result: String
        val privateResult: String?
        val success: Boolean
        val bMember = eventWrapper.botMember!!
        val guildId = eventWrapper.guildId!!
        val currentOwnerId: String? = eventWrapper.botGuild!!.leaderId
        if (bMember.hasBeenForciblyRemoved()) {
            result = "You have been recently removed by administrative action. Wait until someone else is leader."
            eventWrapper.sendResponse(result)
            return
        }
        if (currentOwnerId == null ||
                bot.getMemberWrapper(currentOwnerId, guildId).member == null) { //second state shouldn't happen
            result = "**" + bMember.effectiveName + "** is the first player to declare themselves leader, " +
                    "and is now leader!"
            privateResult = null
            success = true
        } else {
            val currentOwner = bot.getMemberWrapper(currentOwnerId, guildId)
            if (currentOwner == bMember) {
                result = "You realize that you're no match for yourself, and call it off."
                eventWrapper.sendResponse(result)
                return
            }
            val influence = bMember.influence
            val ownerInfluence = currentOwner.influence
            if (influence > ownerInfluence) {
                bMember.adjustInfluence(ownerInfluence.negate())
                currentOwner.adjustInfluence(ownerInfluence.negate())
                result = "**" + bMember.effectiveName + "** has successfully supplanted **" +
                        currentOwner.member!!.asMention + "** as leader, reducing both users' influence and becoming" +
                        " the new leader!"
                privateResult = "*This maneuver has cost you $ownerInfluence influence. " +
                    "${currentOwner.effectiveName} has lost **ALL** of their influence.*"
                success = true
            } else {
                bMember.adjustInfluence(influence.negate())
                currentOwner.adjustInfluence(influence.negate())
                result = "**${bMember.effectiveName}** has attempted a coup on **${currentOwner.member?.asMention}**," +
                        "which has backfired. Both players have lost influence" +
                        " and the leader has not changed."
                privateResult = "*This maneuver has cost you **ALL** of your influence. " +
                        "${currentOwner.effectiveName} has lost $influence of their influence.*"
                success = false
            }
        }
        if (success) {
            bot.getGuildWrapper(guildId).doCoup(eventWrapper.author)
        }
        eventWrapper.sendResponse(result)
        if (privateResult != null) {
            eventWrapper.sendPrivateResponse(privateResult)
        }
    }

    override fun info(authorityLevel: AuthorityLevel, isPublic: Boolean): String? {
        return if (isPublic) {
            (""
                    + "**" + bot.prefix + "coup** - become server owner; this will drain both your influence and the influence " +
                    "of the current owner until one (or both) of you run out. For ties, the existing owner is still owner.")
        } else null
    }
}