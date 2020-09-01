package botmanager.frostbalance.commands.influence

import botmanager.frostbalance.DailyInfluenceSource
import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.WeeklyInfluenceSource
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.ContextLevel
import botmanager.frostbalance.command.FrostbalanceGuildCommand
import botmanager.frostbalance.command.GuildMessageContext
import java.util.*

class SubscribeCommand(bot: Frostbalance?) : FrostbalanceGuildCommand(bot, arrayOf(
        "subscribe"
), AuthorityLevel.GENERIC, ContextLevel.PUBLIC_MESSAGE) {
    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {
        if (context.member.subscribed) {

            context.member.renewSubscription()
            context.sendResponse("Your subscription in " + context.guild.name + " has been renewed.")
        } else {
            context.member.subscribe()
            val response: MutableList<String?> = ArrayList()
            response.add("You have subscribed to " + context.guild.name + " and will gain influence there for a week.")
            response.add("You will gain " + DailyInfluenceSource.DAILY_INFLUENCE_CAP.subtract(WeeklyInfluenceSource.FALLOFF) + " tomorrow, and " + WeeklyInfluenceSource.FALLOFF + " less the day after.")
            response.add("Renew your subscription with `" + bot.prefix + mainAlias + "`.")
            context.sendMultiLineResponse(response)
            context.member.updateSubscription()
        }
    }

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return "**.subscribe** - like `.daily`, but weekly, and slightly worse"
    }
}