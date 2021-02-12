package botmanager.frostbalance.commands.map

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*
import botmanager.frostbalance.resource.ResourceData

class SearchCommand(bot: Frostbalance): FrostbalanceGuildCommand(bot, arrayOf(
    "search"
), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String {
        return "do .search to find stuff"
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        var args = ArgumentStream(params)
        val searches = args.nextInteger()

        if (searches != null) {
            context.player.character.searchTile(searches)
        } else {
            context.player.character.searchTile(1)
        }

        context.sendResponse("You are now searching for resources at ${context.player.character.destination.getCoordinates(context.author.userOptions.coordSys)}, and will stop after $searches attempts.")

    }

}