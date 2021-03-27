package botmanager.frostbalance.commands.resource

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.ContextLevel
import botmanager.frostbalance.command.FrostbalanceGuildCommand
import botmanager.frostbalance.command.GuildMessageContext
import botmanager.frostbalance.grid.building.Workshop

class UnloadBuildingCommand(bot: Frostbalance?) : FrostbalanceGuildCommand(bot, arrayOf("unload"), AuthorityLevel.GENERIC, ContextLevel.ANY) {
    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String {
        return "**.unload** - unload items from the building you are currently on."
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        val targetPlayer = context.player.character
        val targetBuilding = targetPlayer.tile.buildingData.activeBuilding

        if (targetBuilding != null) {
            if (targetBuilding is Workshop) {
                val inputReference = targetBuilding.inputInventory!!.makeFiction()
                for (item in inputReference.items) {
                    targetPlayer.getInventory().addItem(item)
                    targetBuilding.inputInventory!!.removeItem(item)
                }
                val outputReference = targetBuilding.outputInventory!!.makeFiction()
                for (item in outputReference.items) {
                    targetPlayer.getInventory().addItem(item)
                    targetBuilding.outputInventory!!.removeItem(item)
                }
                context.sendResponse("Items unloaded!")
            } else {
                return context.sendResponse("This can only be done on workshops right now.")
            }
        }
    }

}