package botmanager.frostbalance.commands.resource

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*
import botmanager.frostbalance.grid.building.WorkManager
import botmanager.frostbalance.menu.BuildGathererMenu

class WorkCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("work"), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return ".work - Works the gatherer on a tile (if there is one available)"
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        var args = ArgumentStream(params)
        val workCycles = args.nextInteger()

        val character = context.player.character
        val activeGatherer = character.tile.buildingData.activeGatherer() ?: return context.sendResponse("There are no active gatherers here!")

        if (activeGatherer.owner != context.player) return context.sendResponse("You don't own this gatherer!")

        if (workCycles != null) {
            context.player.character.work(workCycles)
        } else {
            context.player.character.work(60)
        }

        return if (WorkManager.singleton.addWorker(activeGatherer, context.player.character)) {
            context.sendResponse("You are now working on your gatherer (${activeGatherer.deposit})  for the next $workCycles minutes after ceasing to work at your previous one.")
        } else {
            context.sendResponse("You are now working on your gatherer (${activeGatherer.deposit}) for the next $workCycles minutes.")
        }

    }

}