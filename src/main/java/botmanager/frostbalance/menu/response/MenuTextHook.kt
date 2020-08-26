package botmanager.frostbalance.menu.response

import botmanager.frostbalance.command.MessageContext
import botmanager.frostbalance.menu.Menu

/**
 * Allows you to interact with menus by typing a text response in the same channel.
 * There can only be one menu per channel per user, and only one text hook per menu.
 */
//TODO limit the bot to one menu per channel per user rather than just saying 'you're gucchi fam' to every lazy-boned menu that shows up
abstract class MenuTextHook(var menu: Menu, name: String) : MenuAction(name) {

    fun readMessage(context: MessageContext) {
        if (isValid(context)) hookEvent(context)
    }

    abstract fun hookEvent(context: MessageContext)
    abstract fun isValid(context: MessageContext): Boolean

}