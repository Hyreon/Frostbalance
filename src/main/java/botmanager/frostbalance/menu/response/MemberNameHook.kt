package botmanager.frostbalance.menu.response

import botmanager.frostbalance.command.MessageContext
import botmanager.frostbalance.menu.Menu

abstract class MemberNameHook(menu: Menu, name: String) : SimpleTextHook(menu, name) {

    override fun isValid(context: MessageContext): Boolean {
        return super.isValid(context) && context.guild?.let {
            context.bot.getUserByName(context.message.contentStripped)
                ?.memberIfIn(it)
        } != null
    }

}
