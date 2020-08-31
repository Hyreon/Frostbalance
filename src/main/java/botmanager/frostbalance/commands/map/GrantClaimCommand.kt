package botmanager.frostbalance.commands.map

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*
import botmanager.frostbalance.menu.ConfirmationMenu

class GrantClaimCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("grant"), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        val argumentStream = ArgumentStream(params)
        val claimData = argumentStream.nextCoordinate()?.let { context.gameNetwork.worldMap.getTile(it).claimData }
                ?: return context.sendResponse("Could not recognize location '${argumentStream.lastArgument}'.")
        val grantAmount = argumentStream.nextInfluence()
                ?: return context.sendResponse("The amount '${argumentStream.lastArgument}' wasn't recognized as a valid influence amount.")
        val targetPlayer = bot.getUserByName(argumentStream.exhaust())?.playerIn(context.gameNetwork)
                ?: return context.sendResponse("Could not find player '${argumentStream.lastArgument}'.")

        if (claimData.getClaim(context.player, context.player.allegiance)?.strength?.let { it < grantAmount} == true ) {
            return context.sendResponse("You don't have $grantAmount of territory to give on this tile! Make sure it's right one, " +
                    "and that you haven't changed allegiance.")
        }

        ConfirmationMenu(bot, context, {
            claimData.getClaim(context.player, context.player.allegiance)?.transferToClaim(targetPlayer, grantAmount)
            context.sendResponse("You have given ${targetPlayer.name} $grantAmount territory units at ${claimData.tile.location}.")
        }, "This player is not in your nation and won't be able to use the claim unless they join your nation! Are you sure?")
                .sendOnCondition(targetPlayer.allegiance != context.player.allegiance)

    }

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return "**.grant LOCATION AMOUNT PLAYER** - grants some portion of your land to a player."
    }

}