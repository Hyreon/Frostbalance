package botmanager.frostbalance.commands.map

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.ContextLevel
import botmanager.frostbalance.command.FrostbalanceGuildCommand
import botmanager.frostbalance.command.GuildMessageContext
import botmanager.frostbalance.grid.building.WorkManager
import botmanager.frostbalance.menu.BuildGathererMenu

class WorkCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("gatherer"), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return ".work - Works the gatherer on a tile (if there is one available)"
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        WorkManager.singleton.addWorker(context.player.character.tile.buildingData.activeGatherer(), context.player.character)

    }

}