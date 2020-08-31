package botmanager.frostbalance.commands.map

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*
import botmanager.frostbalance.menu.ConfirmationMenu

class UnclaimCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("unclaim"), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        val argumentStream = ArgumentStream(params)
        val claimData = argumentStream.nextCoordinate()?.let { context.gameNetwork.worldMap.getTile(it).claimData }
                ?: return context.sendResponse("Could not recognize location '${argumentStream.lastArgument}'.")
        val grantAmount = argumentStream.nextInfluence()
                ?: return context.sendResponse("The amount '${argumentStream.lastArgument}' wasn't recognized as a valid influence amount.")

        if (claimData.getClaim(context.player, context.player.allegiance)?.strength?.let { it < grantAmount} == true ) {
            return context.sendResponse("You don't have $grantAmount of territory to remove on this tile! Make sure it's right one, " +
                    "and that you haven't changed allegiance.")
        }

        ConfirmationMenu(bot, context, {
            val reduceAmount = claimData.reduceClaim(context.player, context.player.allegiance, grantAmount)
            context.sendResponse("You have deleted $reduceAmount territory units at ${claimData.tile.location}.")
        }, "You are about to delete $grantAmount worth of influence. Are you sure?")
                .send()

    }

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return "**.unclaim LOCATION AMOUNT** - Unclaim a tile"
    }

}