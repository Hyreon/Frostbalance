package botmanager.doomstack.menu.response

import botmanager.Utilities
import botmanager.doomstack.MessageContext
import botmanager.doomstack.menu.Menu

abstract class NumberHook(menu: Menu, string: String) : SimpleTextHook(menu, string) {

    override fun isValid(hookContext: MessageContext): Boolean {
        return super.isValid(hookContext) && Utilities.isNumber(hookContext.message.contentRaw)
    }

}
