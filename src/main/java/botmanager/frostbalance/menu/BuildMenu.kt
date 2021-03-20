package botmanager.frostbalance.menu

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.GuildMessageContext
import botmanager.frostbalance.grid.building.Housing
import botmanager.frostbalance.menu.response.DynamicMenuResponse
import botmanager.frostbalance.menu.response.MenuResponse
import net.dv8tion.jda.api.EmbedBuilder

class BuildMenu(bot: Frostbalance, context: GuildMessageContext) : Menu(bot, context) {

    var outputState = BuildState.FAIL

    init {

        menuResponses.add(object : MenuResponse("\uD83C\uDFD7", "Build gatherer") {
            override fun reactEvent() {

                redirectTo(
                    BuildGathererMenu(bot, context),
                true)

            }

            override val isValid: Boolean
                get() = true

        })

        menuResponses.add(object : MenuResponse("\uD83C\uDFE0", "Build housing") {
            override fun reactEvent() {


                context.author.playerIn(context.gameNetwork).let { player ->
                    player.character.tile.let { tile ->
                        tile.buildingData.let { buildData ->
                            if (buildData.housingFor(player) != null) {
                                buildData.activateBuilding(buildData.housingFor(player)!!)
                                outputState = BuildState.REBUILT_HOUSE
                            } else {
                                buildData.addBuilding(Housing(tile, player))
                                outputState = BuildState.BUILT_NEW_HOUSE
                            }
                        }
                    }
                }
                close(false)

            }

            override val isValid: Boolean
                get() = true

        })

        menuResponses.add(object : MenuResponse("\uD83D\uDEE0", "Build worksite") {
            override fun reactEvent() {

                setDelegating(true)
                redirectTo(
                    BuildWorkshopMenu(bot, context),
                    true)

            }

            override val isValid: Boolean
                get() = true

        })

    }

    override val embedBuilder: EmbedBuilder
        get() {
            for (response in menuResponses) {
                if (response is DynamicMenuResponse) {
                    response.updateValues()
                }
            }
            return EmbedBuilder()
                .setTitle(if (isClosed) {
                    when (outputState) {
                        BuildState.BUILT_NEW_HOUSE -> {
                            "You have built a new house."
                        }
                        BuildState.REBUILT_HOUSE -> {
                            "You have reactivated your old house."
                        }
                        else -> {
                            "Something went wrong! Nothing was built. Probably."
                        }
                    }
                } else {
                    "What do you want to build?"
                })
        }

    enum class BuildState {

        BUILT_NEW_HOUSE, REBUILT_HOUSE, FAIL;

    }

}
