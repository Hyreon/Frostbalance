package botmanager.frostbalance.commands.resource

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*
import botmanager.frostbalance.grid.biome.Biome

class DepositListCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("deposits", "resources"), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String {
        return "**.deposits** - List all deposits currently on this tile"
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        val argStream = ArgumentStream(params)

        val resourceData = context.player.character.tile.resourceData
        val lines = resourceData.priorityOrderDeposits().map { ":white_medium_square: ${it.deposit.name} ${it.supply * it.maxSupplyFactor} / ${it.maxSupplyFactor}" }.toMutableList()
        lines.add(":white_small_square: ".repeat(resourceData.progress) + ":black_small_square: ".repeat(resourceData.numResources() + 1 - resourceData.progress) + " *To next resource*")
        lines.add(0, "**Resources discovered here:** ${resourceData.numResources()}")
        context.sendMultiLineResponse(lines)

    }

}