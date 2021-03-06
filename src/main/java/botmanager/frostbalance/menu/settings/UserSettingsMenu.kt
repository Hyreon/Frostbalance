package botmanager.frostbalance.menu.settings

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.MessageContext
import botmanager.frostbalance.grid.coordinate.Hex
import botmanager.frostbalance.menu.Menu
import botmanager.frostbalance.menu.input.BooleanMenu
import botmanager.frostbalance.menu.input.DoubleMenu
import botmanager.frostbalance.menu.option.OptionMenu
import botmanager.frostbalance.menu.response.DynamicMenuResponse
import net.dv8tion.jda.api.EmbedBuilder

class UserSettingsMenu(bot: Frostbalance, context: MessageContext) : Menu(bot, context) {

    init {

        menuResponses.add(object : DynamicMenuResponse({"\uD83E\uDDED"}, {"Simple coordinates: ${context.author.userOptions.coordSys == Hex.CoordSys.APPROXIMATION}"}) {
            override fun reactEvent() {

                redirectTo(BooleanMenu(bot, context, {
                    context.author.userOptions.coordSys = Hex.CoordSys.APPROXIMATION
                }, {
                    context.author.userOptions.coordSys = Hex.CoordSys.NAVIGATOR
                }, "$emoji $name",
                        "When enabled, all coordinates will approximate their position on an x-y graph instead of using the default system. " +
                                "While easier to understand, you may have trouble using it for navigation or for finer " +
                                "movements."),
                        true)

            }

            override val isValid: Boolean
                get() = true

        })

        menuResponses.add(object : DynamicMenuResponse({"\uD83D\uDCD0"}, {"Map Default Zoom: ${context.author.userOptions.zoomSize}"}) {
            override fun reactEvent() {

                redirectTo(DoubleMenu(bot, context, {
                    context.author.userOptions.zoomSize = it
                }, {
                    it in 0.02..5.0
                }, listOf(1.0, 0.8, 0.5), "$emoji $name",
                        "Set the zoom when viewing a `.map`; " +
                                "smaller numbers mean more tiles at once, bigger numbers mean bigger tiles. " +
                                "You can't have a number smaller than 0.2 or bigger than 5."),
                        true)

            }

            override val isValid: Boolean
                get() = true

        })

        menuResponses.add(object : DynamicMenuResponse({"♾️"}, {"Re-queue actions when complete: ${context.author.userOptions.loopActions}"}) {
            override fun reactEvent() {

                redirectTo(BooleanMenu(bot, context, {
                    context.author.userOptions.loopActions = true
                }, {
                    context.author.userOptions.loopActions = false
                }, "$emoji $name",
                        "When enabled, your character will automatically re-do whatever it was told to do " +
                                "before. You can use this to set up patrol routes, or to walk forever."),
                        true)

            }

            override val isValid: Boolean
                get() = true

        })

        menuResponses.add(object : DynamicMenuResponse({"✒️"}, {"Draw coordinates on map: ${context.author.userOptions.drawCoords}"}) {
            override fun reactEvent() {

                redirectTo(BooleanMenu(bot, context, {
                    context.author.userOptions.drawCoords = true
                }, {
                    context.author.userOptions.drawCoords = false
                }, "$emoji $name",
                        "When enabled, coordinates will be drawn on the world map. " +
                                "This is primarily a debug feature, but you may find it helpful for navigation."),
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
                    .setTitle("User Settings")
        }
}