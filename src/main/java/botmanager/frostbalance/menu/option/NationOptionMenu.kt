package botmanager.frostbalance.menu.option

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.Nation
import botmanager.frostbalance.command.CommandContext

abstract class NationOptionMenu(bot: Frostbalance, context: CommandContext, nations: List<Nation>) : OptionMenu<Nation>(bot, context, nations) {

    override fun updatePickResponseText() {
        sublist.forEachIndexed { i, entry -> pickResponses[i].name = entry.toString() }
        sublist.forEachIndexed { i, entry -> pickResponses[i].emoji = entry.emoji }
    }

}