package botmanager.frostbalance.menu

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.GuildMessageContext
import botmanager.frostbalance.grid.building.Gatherer
import botmanager.frostbalance.menu.option.OptionMenu
import botmanager.frostbalance.resource.ResourceDeposit
import net.dv8tion.jda.api.EmbedBuilder

class BuildGathererMenu(bot: Frostbalance, context: GuildMessageContext) :
        OptionMenu<ResourceDeposit>(bot, context, context.player.character.tile.resourceData.priorityOrderDeposits()) {

    override val embedBuilder: EmbedBuilder
        get() {
            if (isClosed) {
                return EmbedBuilder()
                        .setTitle("Build Successful")
                        .setDescription("You've build a gatherer on your tile.")
            } else {
                return EmbedBuilder()
                        .setTitle("Build Gatherer")
                        .setDescription("Select a resource below to build a gatherer for.")
            }
        }

    override fun select(option: ResourceDeposit) {
        context.author.playerIn(context.gameNetwork).character.tile.run {
            buildingData.addGatherer(Gatherer(this, option))
            close(false)
        }
    }

}
