package botmanager.frostbalance.menu.response

import botmanager.Utilities
import botmanager.frostbalance.command.MessageContext
import botmanager.frostbalance.menu.Menu

abstract class NumberHook(menu: Menu, string: String) : SimpleTextHook(menu, string) {

    override fun isValid(hookContext: MessageContext): Boolean {
        return super.isValid(hookContext) && Utilities.isNumber(hookContext.message.contentRaw)
    }

}
