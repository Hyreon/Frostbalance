package botmanager.frostbalance.menu.settings

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.MessageContext
import botmanager.frostbalance.menu.Menu
import botmanager.frostbalance.menu.response.MenuResponse
import net.dv8tion.jda.api.EmbedBuilder

class UserSettingsMenu(bot: Frostbalance, context: MessageContext) : Menu(bot, context) {

    init {

        menuResponses.add(object : MenuResponse("\uD83D\uDCD0", "Map Default Zoom") {
            override fun reactEvent() {

                redirectTo(BooleanMenu(bot, context, {

                    close(false)
                }, {

                    close(false)
                }), true)

            }

            override val isValid: Boolean
                get() = true

        })

    }

    override val embedBuilder: EmbedBuilder
        get() = EmbedBuilder()
                .setTitle("User Settings")
}