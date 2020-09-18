package botmanager.frostbalance.menu.settings

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.MessageContext
import botmanager.frostbalance.menu.Menu
import botmanager.frostbalance.menu.input.DoubleMenu
import botmanager.frostbalance.menu.response.DynamicMenuResponse
import net.dv8tion.jda.api.EmbedBuilder

class UserSettingsMenu(bot: Frostbalance, context: MessageContext) : Menu(bot, context) {

    init {

        menuResponses.add(object : DynamicMenuResponse({"\uD83D\uDCD0"}, {"Map Default Zoom: ${context.author.userOptions.zoomSize}"}) {
            override fun reactEvent() {

                redirectTo(DoubleMenu(bot, context, {
                    context.author.userOptions.zoomSize = it
                }, {
                    it in 0.2..5.0
                }, listOf(1.0, 0.8, 0.5), "$emoji $name",
                        "Set the zoom when viewing a `.map`; " +
                                "smaller numbers mean more tiles at once, bigger numbers mean bigger tiles. " +
                                "You can't have a number smaller than 0.2 or bigger than 5."),
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