package botmanager.frostbalance.menu.settings

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.MessageContext
import botmanager.frostbalance.menu.Menu
import botmanager.frostbalance.menu.input.BooleanMenu
import botmanager.frostbalance.menu.input.DoubleMenu
import botmanager.frostbalance.menu.response.MenuResponse
import net.dv8tion.jda.api.EmbedBuilder

class UserSettingsMenu(bot: Frostbalance, context: MessageContext) : Menu(bot, context) {

    init {

        menuResponses.add(object : MenuResponse("\uD83D\uDCD0", "Map Default Zoom") {
            override fun reactEvent() {

                redirectTo(DoubleMenu(bot, context, {
                    context.author.userOptions.zoomSize = it
                    close(false)
                }, {
                    it in 0.2..5.0
                }, listOf(1.0, 0.8, 0.5), "$emoji $name",
                        "Set the zoom when viewing a `.map`; " +
                                "smaller numbers mean smaller tiles, bigger numbers mean less tiles at once. " +
                                "You can't have a number smaller than 0.2 or bigger than 5."),
                        true)

            }

            override val isValid: Boolean
                get() = true

        })

    }

    override val embedBuilder: EmbedBuilder
        get() = EmbedBuilder()
                .setTitle("User Settings")
}