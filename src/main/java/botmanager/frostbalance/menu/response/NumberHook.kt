package botmanager.frostbalance.menu.response

import botmanager.frostbalance.command.MessageContext
import botmanager.frostbalance.menu.Menu

abstract class NumberHook(menu: Menu, string: String) : SimpleTextHook(menu, string) {

    override fun isValid(hookContext: MessageContext): Boolean {
        return hookContext.channel == menu.originalMenu.message?.channel &&
                hookContext.author == menu.originalMenu.actor
    }

}
