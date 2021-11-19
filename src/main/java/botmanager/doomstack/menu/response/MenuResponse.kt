package botmanager.doomstack.menu.response

import botmanager.frostbalance.menu.response.MenuAction

abstract class MenuResponse internal constructor(var emoji: String, name: String): MenuAction(name) {

    fun applyReaction() {
        if (isValid) reactEvent()
    }

    abstract fun reactEvent()
    abstract val isValid: Boolean

}