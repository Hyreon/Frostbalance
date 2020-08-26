package botmanager.frostbalance.menu.response

abstract class MenuAction(var name: String) {
    fun applyReaction() {
        if (isValid) reactEvent()
    }

    abstract fun reactEvent()
    abstract val isValid: Boolean
}