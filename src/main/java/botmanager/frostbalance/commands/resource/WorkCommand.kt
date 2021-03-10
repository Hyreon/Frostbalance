package botmanager.frostbalance.commands.resource

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*
import botmanager.frostbalance.grid.building.WorkManager
import botmanager.frostbalance.menu.BuildGathererMenu

class WorkCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("work"), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return "**.work MINUTES** - Works the gatherer on a tile (if there is one available)"
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        var args = ArgumentStream(params)
        val workCycles = args.nextInteger() ?: 60

        context.player.character.work(workCycles)
        context.sendResponse("If possible, you will now work at ${context.player.character.destination.getCoordinates(context.author.userOptions.coordSys)}, and will stop after $workCycles minutes.")

    }

}