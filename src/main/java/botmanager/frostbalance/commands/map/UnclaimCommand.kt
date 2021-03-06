package botmanager.frostbalance.commands.map

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*
import botmanager.frostbalance.menu.input.ConfirmationMenu

class UnclaimCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("unclaim"), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        val argumentStream = ArgumentStream(params)
        val claimData = argumentStream.nextCoordinate()?.let { context.gameNetwork.worldMap.getTile(it).claimData }
                ?: return context.sendResponse("Could not recognize location '${argumentStream.lastArgument}'.")
        val grantAmount = argumentStream.nextInfluence()
                ?: return context.sendResponse("The amount '${argumentStream.lastArgument}' wasn't recognized as a valid influence amount.")

        if (claimData.getClaim(context.player, context.player.allegiance)?.investedStrength?.let { it < grantAmount} != false) {
            return context.sendResponse("You don't have $grantAmount of territory to remove on this tile! Make sure it's right one, " +
                    "and that you haven't changed allegiance.")
        }

        ConfirmationMenu(bot, context, {
            val amountPair = claimData.reduceClaim(context.player, context.guild.nation, grantAmount)
            context.sendPrivateResponse("You have been refunded ${amountPair.first} influence because you had not finalized that much.")
            context.sendResponse("You have deleted ${amountPair.first + amountPair.second} territory units at ${claimData.tile.location}.")
        }, "You are about to delete $grantAmount worth of influence. Are you sure?")
                .send()

    }

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String {
        return "**.unclaim LOCATION AMOUNT** - Unclaim a tile."
    }

}