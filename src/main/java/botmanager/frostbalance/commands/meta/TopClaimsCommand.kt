package botmanager.frostbalance.commands.meta

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.ContextLevel
import botmanager.frostbalance.command.FrostbalanceGuildCommand
import botmanager.frostbalance.command.GuildMessageContext

class TopClaimsCommand(bot: Frostbalance?) : FrostbalanceGuildCommand(bot, arrayOf(
        "claimtop"
), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {
        context.sendResponse("${context.gameNetwork.worldMap.highestLevelClaim.claimLevel}")
    }

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return "**.claimtop** - Get the players with the most leveled claims\n" +
                "**.claimtop NATION** - Get the nations with the most claims (does not count internal clash)"
    }
}