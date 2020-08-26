package botmanager.frostbalance.menu.response

import botmanager.frostbalance.command.MessageContext
import botmanager.frostbalance.menu.Menu

abstract class SimpleTextHook(menu: Menu, string: String) : MenuTextHook(menu, string) {

    override fun isValid(context: MessageContext): Boolean {
        return context.channel == menu.originalMenu.message?.channel &&
                context.author == menu.originalMenu.actor
    }

}
