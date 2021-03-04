package botmanager.frostbalance.commands.resource

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.ContextLevel
import botmanager.frostbalance.command.FrostbalanceGuildCommand
import botmanager.frostbalance.command.GuildMessageContext
import java.lang.Math.round
import kotlin.math.roundToInt

class DepositOddsCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("checkodds", "odds"), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return ".checkodds - Czech odds of finding resources on your current tile"
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        var odds = Frostbalance.bot.resourceOddsFor(context.player.character.tile.biome)
        context.sendMultiLineResponse(odds.map { x -> x.first.toString() + ": " + String.format("%.1f", x.second * 100) + "%" } )

    }

}