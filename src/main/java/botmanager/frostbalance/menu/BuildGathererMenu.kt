package botmanager.frostbalance.menu

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.GuildMessageContext
import botmanager.frostbalance.grid.building.Gatherer
import botmanager.frostbalance.menu.option.OptionMenu
import botmanager.frostbalance.resource.ResourceDeposit
import net.dv8tion.jda.api.EmbedBuilder

class BuildGathererMenu(bot: Frostbalance, context: GuildMessageContext) :
        OptionMenu<ResourceDeposit>(bot, context, context.player.character.tile.resourceData.priorityOrderDeposits()) {

    var success = false
    var newGatherer = false

    override val embedBuilder: EmbedBuilder
        get() {
            val baseEmbedBuilder = super.embedBuilder
            if (isClosed) {
                return if (success) {
                    if (newGatherer)
                        baseEmbedBuilder
                                .setTitle("Build Successful")
                                .setDescription("You've built a gatherer on your tile.")
                    else
                        baseEmbedBuilder
                                .setTitle("Repair Successful")
                                .setDescription("You've reinstated your previous gatherer on your tile.")
                } else {
                    baseEmbedBuilder
                            .setTitle("Build Canceled")
                }
            } else {
                return baseEmbedBuilder
                        .setTitle("Build Gatherer")
                        .setDescription("Select a resource below to build a gatherer for.")
            }
        }

    override fun select(option: ResourceDeposit) {
        context.author.playerIn(context.gameNetwork).let { player ->
            player.character.tile.let { tile ->
                tile.buildingData.let { buildData ->
                    if (buildData.gathererOf(option) != null) {
                        buildData.activateBuilding(buildData.gathererOf(option)!!)
                        newGatherer = false
                    } else {
                        buildData.addBuilding(Gatherer(tile, player, option))
                        newGatherer = true
                    }
                }
            }
        }
        success = true
        close(false)
    }

}
