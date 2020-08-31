package botmanager.frostbalance.commands.map

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*
import botmanager.frostbalance.menu.ConfirmationMenu

class EvictCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("evict"), AuthorityLevel.NATION_LEADER, ContextLevel.ANY) {

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        val argumentStream = ArgumentStream(params)
        val claimData = argumentStream.nextCoordinate()?.let { context.gameNetwork.worldMap.getTile(it).claimData }
                ?: return context.sendResponse("Could not recognize location '${argumentStream.lastArgument}'.")
        val targetPlayer = bot.getUserByName(argumentStream.exhaust())?.playerIn(context.gameNetwork)
                ?: return context.sendResponse("Could not find player '${argumentStream.lastArgument}'.")
        val claim = claimData.getClaim(context.player, context.player.allegiance)
                ?: return context.sendResponse("${targetPlayer.name} doesn't have any claim for your nation at ${claimData.tile.location}.")

        if (!claim.strength.nonZero) {
            return context.sendResponse("${targetPlayer.name} has already been evicted from this tile!")
        }

        ConfirmationMenu(bot, context, {
            claim.evict()
            context.sendResponse("You have evicted ${targetPlayer.name} at ${claimData.tile.location}.")
        }, "${targetPlayer.name} will lose all claims to ${claimData.tile.location}, and your nation will lose its strength on this " +
                "tile. You can always reverse this, but you can't make them forgive you too!")
                .send()

    }

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return "**.evict LOCATION PLAYER** - evict someone from their land"
    }

}