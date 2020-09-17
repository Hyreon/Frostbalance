package botmanager.frostbalance.menu.input

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.MessageContext
import botmanager.frostbalance.menu.Menu
import botmanager.frostbalance.menu.response.MenuResponse
import net.dv8tion.jda.api.EmbedBuilder

class BooleanMenu(bot: Frostbalance, context: MessageContext,
                  accept: () -> Unit, deny: () -> Unit,
                  private val title: String,
                  private val description: String =
                          "Toggle this on or off with the emoji responses below.")
    : Menu(bot, context) {

    init {
        menuResponses.add(object : MenuResponse("✅", "Confirm") {
            override fun reactEvent() {
                accept()
                close(false)
            }

            override val isValid: Boolean
                get() = true
        })

        menuResponses.add(object : MenuResponse("❎", "Cancel") {
            override fun reactEvent() {
                deny()
                close(false)
            }

            override val isValid: Boolean
                get() = true
        })
    }

    override val embedBuilder: EmbedBuilder
        get() {
            return EmbedBuilder()
                    .setTitle(title)
                    .setDescription(description)
        }

}
