package botmanager.frostbalance.commands.influence

import botmanager.frostbalance.DailyInfluenceSource
import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.WeeklyInfluenceSource
import botmanager.frostbalance.command.*
import java.util.*

class UnsubscribeCommand(bot: Frostbalance) : FrostbalanceGuildCommand(
        bot, arrayOf("unsubscribe"), AuthorityLevel.GENERIC, ContextLevel.PUBLIC_MESSAGE
) {

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String?) {

        if (context.member.subscribed) {

            context.member.updateSubscription()
            context.member.unsubscribe()
            context.sendResponse("You have unsubscribed from " + context.guild.name + ".")

        } else {

            context.sendResponse("You aren't subscribed in " + context.guild.name + ".")

        }

    }

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return "**.unsubscribe** - unsubscribe."
    }

}
