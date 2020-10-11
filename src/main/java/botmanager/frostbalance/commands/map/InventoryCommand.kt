package botmanager.frostbalance.commands.map

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.ContextLevel
import botmanager.frostbalance.command.FrostbalanceGuildCommand
import botmanager.frostbalance.command.GuildMessageContext

class InventoryCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("inventory"), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return ".inventory - Czech inventory"
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        context.sendResponse(context.player.character.getInventory().render())

    }

}