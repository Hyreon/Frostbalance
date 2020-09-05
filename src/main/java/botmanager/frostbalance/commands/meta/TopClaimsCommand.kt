package botmanager.frostbalance.commands.meta

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.Influence
import botmanager.frostbalance.Player
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.ContextLevel
import botmanager.frostbalance.command.FrostbalanceGuildCommand
import botmanager.frostbalance.command.GuildMessageContext
import botmanager.frostbalance.menu.option.ListMenu

class TopClaimsCommand(bot: Frostbalance?) : FrostbalanceGuildCommand(bot, arrayOf(
        "claimtop"
), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {
        val scores = context.gameNetwork.players.associateWith { player ->
            context.gameNetwork.worldMap.loadedTiles.map { tile -> tile.claimData }
                    .filter { claimData -> claimData.owningPlayer == player }
                    .map { claimData -> claimData.claimLevel }
                    .reduce { acc, level -> acc + level }
        }
                .entries
                .sortedByDescending { it.value }
                .map {
            "${it.key.name}: ${Influence(it.value)}"
        }
        object : ListMenu<String>(bot, context, scores) {

        }.send()
    }

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return "**.claimtop** - Get the players with the most leveled claims\n" +
                "**.claimtop NATION** - Get the nations with the most claims (does not count internal clash)"
    }
}