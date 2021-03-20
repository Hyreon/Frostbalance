package botmanager.frostbalance.menu

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.GuildMessageContext
import botmanager.frostbalance.grid.building.Workshop
import botmanager.frostbalance.grid.building.WorkshopType
import botmanager.frostbalance.menu.option.OptionMenu
import net.dv8tion.jda.api.EmbedBuilder

class BuildWorkshopMenu(bot: Frostbalance, context: GuildMessageContext) :
        OptionMenu<WorkshopType>(bot, context, context.bot.workshops) {

    var success = false
    var newWorksite = false

    override val embedBuilder: EmbedBuilder
        get() {
            val baseEmbedBuilder = super.embedBuilder
            if (isClosed) {
                return if (success) {
                    if (newWorksite)
                        baseEmbedBuilder
                                .setTitle("Build Successful")
                                .setDescription("You've built a workshop on your tile.")
                    else
                        baseEmbedBuilder
                                .setTitle("Repair Successful")
                                .setDescription("You've reinstated your previous workshop on your tile.")
                } else {
                    baseEmbedBuilder
                            .setTitle("Build Canceled")
                }
            } else {
                return baseEmbedBuilder
                        .setTitle("Build Workshop")
                        .setDescription("Select the sort of workshop you wish to build.")
            }
        }

    override fun select(option: WorkshopType) {
        context.author.playerIn(context.gameNetwork).let { player ->
            player.character.tile.let { tile ->
                tile.buildingData.let { buildData ->
                    if (buildData.worksiteOf(option)?.owner == player) {
                        buildData.activateBuilding(buildData.worksiteOf(option)!!)
                        newWorksite = false
                    } else {
                        buildData.addBuilding(Workshop(tile, player, option))
                        newWorksite = true
                    }
                }
            }
        }
        success = true
        close(false)
    }

}
