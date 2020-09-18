package botmanager.frostbalance.menu.response

import botmanager.frostbalance.command.MessageContext
import botmanager.frostbalance.menu.Menu

abstract class SimpleTextHook(menu: Menu, string: String) : MenuTextHook(menu, string) {

    override fun isValid(hookContext: MessageContext): Boolean {
        return hookContext.channel == menu.originalMenu.message?.channel &&
                hookContext.author == menu.originalMenu.actor
    }

}
