package botmanager.frostbalance.commands.influence

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.ContextLevel
import botmanager.frostbalance.command.FrostbalanceGuildCommand
import botmanager.frostbalance.command.GuildCommandContext
import botmanager.frostbalance.menu.ConfirmationMenu

class CoupCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf(
        "coup"
), AuthorityLevel.GENERIC, ContextLevel.PUBLIC_MESSAGE) {

    override fun executeWithGuild(context: GuildCommandContext, vararg params: String) {
        var result: String
        var privateResult: String?
        val bMember = context.member
        val guildId = context.guild.id
        val currentOwnerId: String? = context.guild.leaderId
        if (bMember.hasBeenForciblyRemoved()) {
            result = "You have been recently removed by administrative action. Wait until someone else is leader."
            context.sendEmbedResponse(listOf(result))
            return
        }
        if (currentOwnerId == null ||
                bot.getMemberWrapper(currentOwnerId, guildId).member == null) { //second state shouldn't happen
            result = "**" + bMember.effectiveName + "** is the first player to declare themselves leader, " +
                    "and is now leader!"
            context.sendEmbedResponse(listOf(result))
            bot.getGuildWrapper(guildId).doCoup(context.jdaMember)
            return
        } else {
            val currentLeader = bot.getMemberWrapper(currentOwnerId, guildId)
            if (currentLeader == bMember) {
                result = "You realize that you're no match for yourself, and call it off."
                context.sendEmbedResponse(listOf(result))
                return
            }
            val influence = bMember.influence
            val leaderInfluence = currentLeader.influence
            ConfirmationMenu(bot, context, {
                if (influence > leaderInfluence) {
                    bMember.adjustInfluence(leaderInfluence.negate())
                    currentLeader.adjustInfluence(leaderInfluence.negate())
                    result = "**" + bMember.effectiveName + "** has successfully supplanted **" +
                            currentLeader.member?.asMention + "** as leader, reducing both users' influence and becoming" +
                            " the new leader!"
                    privateResult = "*This maneuver has cost you $leaderInfluence influence. " +
                            "${currentLeader.effectiveName} has lost **ALL** of their influence.*"
                    bot.getGuildWrapper(guildId).doCoup(context.jdaMember)
                } else {
                    bMember.adjustInfluence(influence.negate())
                    currentLeader.adjustInfluence(influence.negate())
                    result = "**${bMember.effectiveName}** has attempted a coup on **${currentLeader.member?.asMention}**, " +
                            "which has backfired. Both players have lost influence" +
                            " and the leader has not changed."
                    privateResult = "*This maneuver has cost you **ALL** of your influence. " +
                            "${currentLeader.effectiveName} has lost $influence of their influence.*"
                }
                context.sendEmbedResponse(listOf(result))
                context.sendPrivateResponse(privateResult)
            }, "Are you sure you want to do this? You will lose as much influence as the other user has, and permanently change the course of history!")
        }
    }

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return if (isPublic) {
            (""
                    + "**" + bot.prefix + "coup** - become server owner; this will drain both your influence and the influence " +
                    "of the current owner until one (or both) of you run out. For ties, the existing owner is still owner.")
        } else null
    }
}