package botmanager.frostbalance.menu.settings

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.MessageContext
import botmanager.frostbalance.menu.Menu
import net.dv8tion.jda.api.EmbedBuilder

class BooleanMenu(bot: Frostbalance, context: MessageContext, accept: () -> Unit, deny: () -> Unit) : Menu(bot, context) {

    init {
        
    }

    override val embedBuilder: EmbedBuilder
        get() = TODO("Not yet implemented")

}
