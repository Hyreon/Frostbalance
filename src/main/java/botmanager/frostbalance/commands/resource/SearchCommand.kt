package botmanager.frostbalance.commands.resource

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*
import botmanager.frostbalance.flags.NetworkFlag
import botmanager.frostbalance.resource.ResourceData

class SearchCommand(bot: Frostbalance): FrostbalanceGuildCommand(bot, arrayOf(
    "search"
), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String {
        return "**.search ITEMS** - look for resources on your current tile"
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        val args = ArgumentStream(params)
        val searches = args.nextInteger() ?: 1

        if (!context.gameNetwork.hasNetworkFlag(NetworkFlag.EXPERIMENTAL))
            return context.sendResponse("Resources are only available on the test world for now.")

        context.player.character.searchTile(searches)

        context.sendResponse("You are now searching for resources at ${context.player.character.destination.getCoordinates(context.author.userOptions.coordSys)}, and will stop when $searches  item(s) are found.")

    }

}