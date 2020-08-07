package botmanager.frostbalance.commands.influence

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.FrostbalanceGuildCommandBase
import botmanager.frostbalance.command.GuildCommandContext

class CoupCommand(bot: Frostbalance) : FrostbalanceGuildCommandBase(bot, arrayOf(
        "coup"
), AuthorityLevel.GENERIC, Condition.PUBLIC) {

    override fun executeWithGuild(context: GuildCommandContext, vararg params: String) {
        val result: String
        val privateResult: String?
        val success: Boolean
        val bMember = context.member
        val guildId = context.guild.id
        val currentOwnerId: String? = context.guild.leaderId
        if (bMember.hasBeenForciblyRemoved()) {
            result = "You have been recently removed by administrative action. Wait until someone else is leader."
            context.sendResponse(result)
            return
        }
        if (currentOwnerId == null ||
                bot.getMemberWrapper(currentOwnerId, guildId).member == null) { //second state shouldn't happen
            result = "**" + bMember.effectiveName + "** is the first player to declare themselves leader, " +
                    "and is now leader!"
            privateResult = null
            success = true
        } else {
            val currentLeader = bot.getMemberWrapper(currentOwnerId, guildId)
            if (currentLeader == bMember) {
                result = "You realize that you're no match for yourself, and call it off."
                context.sendResponse(result)
                return
            }
            val influence = bMember.influence
            val leaderInfluence = currentLeader.influence
            if (influence > leaderInfluence) {
                bMember.adjustInfluence(leaderInfluence.negate())
                currentLeader.adjustInfluence(leaderInfluence.negate())
                result = "**" + bMember.effectiveName + "** has successfully supplanted **" +
                        currentLeader.member?.asMention + "** as leader, reducing both users' influence and becoming" +
                        " the new leader!"
                privateResult = "*This maneuver has cost you $leaderInfluence influence. " +
                    "${currentLeader.effectiveName} has lost **ALL** of their influence.*"
                success = true
            } else {
                bMember.adjustInfluence(influence.negate())
                currentLeader.adjustInfluence(influence.negate())
                result = "**${bMember.effectiveName}** has attempted a coup on **${currentLeader.member?.asMention}**," +
                        "which has backfired. Both players have lost influence" +
                        " and the leader has not changed."
                privateResult = "*This maneuver has cost you **ALL** of your influence. " +
                        "${currentLeader.effectiveName} has lost $influence of their influence.*"
                success = false
            }
        }
        if (success) {
            bot.getGuildWrapper(guildId).doCoup(context.jdaMember)
        }
        context.sendResponse(result)
        if (privateResult != null) {
            context.sendPrivateResponse(privateResult)
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