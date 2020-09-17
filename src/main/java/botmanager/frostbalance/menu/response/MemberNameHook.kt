package botmanager.frostbalance.menu.response

import botmanager.frostbalance.command.MessageContext
import botmanager.frostbalance.menu.Menu

abstract class MemberNameHook(menu: Menu, name: String) : SimpleTextHook(menu, name) {

    override fun isValid(hookContext: MessageContext): Boolean {
        return super.isValid(hookContext) && hookContext.guild?.let {
            hookContext.bot.getUserByName(hookContext.message.contentStripped, hookContext.guild)
                ?.memberIfIn(it)
        } != null
    }

}
