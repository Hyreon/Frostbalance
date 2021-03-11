package botmanager.frostbalance.commands.resource

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*
import botmanager.frostbalance.grid.biome.Biome
import java.lang.Math.round
import kotlin.math.roundToInt

class DepositOddsCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("checkodds", "odds"), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String {
        return ".checkodds [BIOME] - Czech odds of finding resources on your current tile"
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        val argStream = ArgumentStream(params)
        var biome = Biome.fromName(argStream.exhaust())
        if (biome == Biome.UNKNOWN) biome = context.player.character.tile.biome

        var odds = Frostbalance.bot.resourceOddsFor(biome)
        odds = odds.sortedByDescending { x -> x.second }
        val lines = odds.map { x -> x.first.toString() + ": " + String.format("%.1f", x.second * 100) + "%" }.toMutableList()
        lines.add("**Abundance: 10.0%**")
        lines.add(0, "Odds for finding resources in ${biome.name}:")
        context.sendMultiLineResponse(lines)

    }

}