package botmanager.doomstack.menu.response

import botmanager.doomstack.MessageContext
import botmanager.doomstack.menu.Menu

abstract class MemberNameHook(menu: Menu, name: String) : SimpleTextHook(menu, name) {

    override fun isValid(hookContext: MessageContext): Boolean {
        return super.isValid(hookContext) && hookContext.guild?.let { guild ->
            hookContext.bot.getUserByName(hookContext.message.contentStripped, hookContext.guild)?.let { user ->
                guild.getMember(
                    user
                )
            }
        } != null
    }

}
