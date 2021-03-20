package botmanager.frostbalance.grid.building

import botmanager.Utilities
import botmanager.frostbalance.Player
import botmanager.frostbalance.grid.Tile
import botmanager.frostbalance.resource.Inventory
import botmanager.frostbalance.resource.crafting.CraftingRecipe
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream

class Workshop(tile: Tile, owner: Player, type: WorkshopType) : Building(tile, owner) {

    var inputInventory: Inventory? = null
    var outputInventory: Inventory? = null
    var currentRecipe: CraftingRecipe? = null
        set(currentRecipe) {
            if (this.currentRecipe != currentRecipe) {
                field = currentRecipe
                progress = 0
            }
        }
    var progress //integer representing the turns spent on this recipe
            = 0

    var typeName: String = type.name

    fun getWorksiteType(): WorkshopType? {
        return tile.map.gameNetwork.bot.workshops.firstOrNull { it.name == typeName }
    }

    override fun doTurn(turn: Long): Boolean {

        val workers = WorkManager.singleton.getWorkers(this)
        val quantity: Int = (workers.size * (1 + level)).toInt()
        for (worker in workers) {
            var experienceGained = false
            progress += quantity
            while (currentRecipe?.baseTurns?.let { it <= progress } == true) {
                if (currentRecipe?.craft(inputInventory, outputInventory) == true && !experienceGained) {
                    gainExperience()
                    experienceGained = true
                }
                progress -= currentRecipe?.baseTurns ?: progress
            }
        }
        return false //never any graphical change

    }

    override fun amountToLevelUp(): Double {
        return 360.0
    }

    override fun getRender(): InputStream? {
        return try {
            FileInputStream(Utilities.getResource("textures/worksite.png"))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
    }

}