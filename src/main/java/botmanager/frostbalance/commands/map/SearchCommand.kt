package botmanager.frostbalance.commands.map

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.ContextLevel
import botmanager.frostbalance.command.FrostbalanceGuildCommand
import botmanager.frostbalance.command.GuildMessageContext
import botmanager.frostbalance.resource.ResourceData

public class SearchCommand(bot: Frostbalance): FrostbalanceGuildCommand(bot, arrayOf(
    "search"
), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return "do .search to find stuff"
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String?) {

        val success = context.player.character.tile.resourceData.search()
        val resourcesFound = context.player.character.tile.resourceData.priorityOrderDeposits()
        ResourceData.simp(resourcesFound)

        context.sendMultiLineResponse(listOf(
                "Success: $success",
                "Resources found: $resourcesFound",
                "Progress: " + context.player.character.tile.resourceData.progress
        ))

    }

}