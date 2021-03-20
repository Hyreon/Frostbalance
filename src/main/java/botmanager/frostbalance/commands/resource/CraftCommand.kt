package botmanager.frostbalance.commands.resource

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*
import botmanager.frostbalance.grid.building.Workshop
import botmanager.frostbalance.menu.option.OptionMenu
import botmanager.frostbalance.resource.crafting.CraftingRecipe

class CraftCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("craft"), AuthorityLevel.GENERIC, ContextLevel.ANY) {
    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String {
        return "**.craft** - Show the crafting menu"
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        val activeBuilding = context.gameNetwork.worldMap.getTile(context.player.character.destination).buildingData.activeBuilding

        if (activeBuilding !is Workshop) {
            return context.sendResponse("You can't craft unless you're at a workshop, or headed towards one.")
        } else {
            context.sendResponse("Sorry, crafting doesn't exist yet. If it did, you could craft any of: ")
            val recipes = activeBuilding.getWorksiteType()?.validRecipes ?: emptyList()
            val recipeMenu = object : OptionMenu<CraftingRecipe>(bot, context, recipes) {

                override fun select(option: CraftingRecipe) {
                    activeBuilding.currentRecipe = option
                }

            }.send()

        }

    }
}