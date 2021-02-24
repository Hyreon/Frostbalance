package botmanager.frostbalance.commands.map

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*
import botmanager.frostbalance.resource.ResourceData

class SearchCommand(bot: Frostbalance): FrostbalanceGuildCommand(bot, arrayOf(
    "search"
), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String {
        return "**.search AMOUNT** - look for resources on your current tile"
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        var args = ArgumentStream(params)
        val searches = args.nextInteger()

        if (!context.gameNetwork.isTutorial())
            context.sendResponse("Resources are only available on the test world for now.")

        if (searches != null) {
            context.player.character.searchTile(searches)
        } else {
            context.player.character.searchTile(1)
        }

        context.sendResponse("You are now searching for resources at ${context.player.character.destination.getCoordinates(context.author.userOptions.coordSys)}, and will stop after $searches attempts.")

    }

}