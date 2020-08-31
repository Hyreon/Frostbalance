package botmanager.frostbalance.commands.map

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*
import botmanager.frostbalance.menu.ConfirmationMenu

class UnevictCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("unevict"), AuthorityLevel.NATION_LEADER, ContextLevel.ANY) {

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        val argumentStream = ArgumentStream(params)
        val claimData = argumentStream.nextCoordinate()?.let { context.gameNetwork.worldMap.getTile(it).claimData }
                ?: return context.sendResponse("Could not recognize location '${argumentStream.lastArgument}'.")
        val targetPlayer = bot.getUserByName(argumentStream.exhaust())?.playerIn(context.gameNetwork)
                ?: return context.sendResponse("Could not find player '${argumentStream.lastArgument}'.")
        val claim = claimData.getClaim(context.player, context.player.allegiance)
                ?: return context.sendResponse("${targetPlayer.name} doesn't have any claim for your nation at ${claimData.tile.location}.")

        if (claim.investedStrength == claim.strength) {
            return context.sendResponse("${targetPlayer.name} hasn't been evicted at this tile!")
        }

        claim.unevict()
        context.sendResponse("You have restored ${targetPlayer.name}'s claim at ${claimData.tile.location}.")

    }

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return "**.evict LOCATION PLAYER** - evict someone from their land"
    }

}