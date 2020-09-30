package botmanager.frostbalance.menu.option

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.Nation
import botmanager.frostbalance.command.MessageContext

abstract class NationOptionMenu(bot: Frostbalance, context: MessageContext, nations: List<Nation>, private val useName: Boolean = false) : OptionMenu<Nation>(bot, context, nations) {

    override fun updatePickResponseText() {
        sublist.forEachIndexed { i, entry -> if (!useName) pickResponses[i].name = entry.toString() else pickResponses[i].name = context.gameNetwork.guildWithAllegiance(entry).toString() }
        sublist.forEachIndexed { i, entry -> pickResponses[i].emoji = entry.emoji }
    }

}