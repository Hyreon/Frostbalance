package botmanager.frostbalance.commands.resource

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.ContextLevel
import botmanager.frostbalance.command.FrostbalanceGuildCommand
import botmanager.frostbalance.command.GuildMessageContext
import botmanager.frostbalance.grid.building.Workshop

class LoadBuildingCommand(bot: Frostbalance?) : FrostbalanceGuildCommand(bot, arrayOf("load"), AuthorityLevel.GENERIC, ContextLevel.ANY) {
    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String {
        return "**.load** - load items into the building you are currently on."
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        val targetPlayer = context.player.character
        val targetBuilding = targetPlayer.tile.buildingData.activeBuilding

        if (targetBuilding != null) {
            if (targetBuilding is Workshop) {
                val inventoryReference = targetPlayer.getInventory().makeFiction()
                for (item in inventoryReference.items) {
                    targetBuilding.inputInventory!!.addItem(item)
                    targetPlayer.getInventory().removeItem(item)
                }
                context.sendResponse("Your entire inventory has been unloaded.")
            } else {
                return context.sendResponse("This can only be done on workshops right now.")
            }
        }
    }

}