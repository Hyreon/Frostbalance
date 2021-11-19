package botmanager.doomstack.menu.settings

import botmanager.doomstack.DoomStack
import botmanager.doomstack.MessageContext
import botmanager.doomstack.menu.Menu
import botmanager.doomstack.menu.response.DynamicMenuResponse
import net.dv8tion.jda.api.EmbedBuilder

class UserSettingsMenu(bot: DoomStack, context: MessageContext) : Menu(bot, context) {

    init {

        //nothing lol

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