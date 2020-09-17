package botmanager.frostbalance.menu.response

import botmanager.frostbalance.command.MessageContext
import botmanager.frostbalance.menu.Menu

/**
 * Allows you to interact with menus by typing a text response in the same channel.
 * There can only be one menu per channel per user, and only one text hook per menu.
 */
abstract class MenuTextHook(var menu: Menu, name: String) : MenuAction(name) {

    fun readMessage(hookContext: MessageContext) {
        if (isValid(hookContext)) hookEvent(hookContext)
    }

    abstract fun hookEvent(hookContext: MessageContext)
    abstract fun isValid(hookContext: MessageContext): Boolean

}