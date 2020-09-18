package botmanager.frostbalance.commands.influence

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.ContextLevel
import botmanager.frostbalance.command.FrostbalanceGuildCommand
import botmanager.frostbalance.command.GuildMessageContext
import java.text.SimpleDateFormat
import java.util.*

class DailyRewardCommand(bot: Frostbalance?) : FrostbalanceGuildCommand(bot, arrayOf(
        "daily"
), AuthorityLevel.GENERIC, ContextLevel.PUBLIC_MESSAGE) {
    var hours = SimpleDateFormat("HH")
    public override fun executeWithGuild(context: GuildMessageContext, params: Array<String>) {

        if (!context.guild.allows(context.player)) {
            return context.sendResponse(context.guild.notAllowed)
        }

        val gain = context.member.gainDailyInfluence()
        if (gain.value > 0) {
            context.sendResponse(context.member.effectiveName + ", your influence increases in " + context.guild.name + ".")
            val influence = context.member.influence
            context.sendPrivateResponse("You now have **" + String.format("%s", influence) + "** influence in **" + context.guild.name + "**.")
        } else {
            val hrsDelay = 24 - hours.format(Date()).toInt()
            context.sendResponse(context.member.effectiveName + ", try again at midnight EST "
                    + "(around " + hrsDelay + " hour" + (if (hrsDelay > 1) "s" else "") + ").")
        }
    }

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return if (isPublic) {
            "**" + bot.prefix + "daily** - gives you all the influence you can get today, instantly"
        } else {
            null
        }
    }
}