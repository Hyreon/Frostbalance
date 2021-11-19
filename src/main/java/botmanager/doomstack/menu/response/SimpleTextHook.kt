package botmanager.doomstack.menu.response

import botmanager.doomstack.MessageContext
import botmanager.doomstack.menu.Menu

abstract class SimpleTextHook(menu: Menu, string: String) : MenuTextHook(menu, string) {

    override fun isValid(hookContext: MessageContext): Boolean {
        return hookContext.channel == menu.originalMenu.message?.channel &&
                hookContext.author == menu.originalMenu.actor
    }

}
