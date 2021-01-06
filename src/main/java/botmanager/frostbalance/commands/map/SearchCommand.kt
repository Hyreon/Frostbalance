package botmanager.frostbalance.commands.map

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*
import botmanager.frostbalance.resource.ResourceData

class SearchCommand(bot: Frostbalance): FrostbalanceGuildCommand(bot, arrayOf(
    "search"
), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return "do .search to find stuff"
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String?) {

        val success =
        if (params.getOrNull(0)?.equals("sudo") == true) {
            context.player.character.tile.resourceData.search(true)
        } else {
            context.player.character.tile.resourceData.search(false)
        }

        val resourcesFound = context.player.character.tile.resourceData.priorityOrderDeposits()
        ResourceData.simp(resourcesFound)

        context.sendMultiLineResponse(listOf(
                "Success: $success",
                "Resources found: $resourcesFound",
                "Progress: " + context.player.character.tile.resourceData.progress
        ))

    }

}