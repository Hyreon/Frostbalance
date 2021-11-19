package botmanager.doomstack.menu.input

import botmanager.doomstack.DoomStack
import botmanager.doomstack.MessageContext
import botmanager.doomstack.menu.Menu
import botmanager.doomstack.menu.response.MenuResponse
import net.dv8tion.jda.api.EmbedBuilder

class BooleanMenu(bot: DoomStack, context: MessageContext,
                  accept: () -> Unit, deny: () -> Unit,
                  private val title: String,
                  private val description: String =
                          "Toggle this on or off with the emoji responses below.")
    : Menu(bot, context) {

    init {
        menuResponses.add(object : MenuResponse("✅", "Yes") {
            override fun reactEvent() {
                accept()
                close(false)
            }

            override val isValid: Boolean
                get() = true
        })

        menuResponses.add(object : MenuResponse("❎", "No") {
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
