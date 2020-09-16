package botmanager.frostbalance.flags

import botmanager.frostbalance.GuildWrapper
import botmanager.frostbalance.UserWrapper
import botmanager.frostbalance.grid.Containable

class UserOptions(@Transient var user: UserWrapper) : Containable<UserWrapper> {

    var zoomSize: Double? = null
        get() = field ?: 1.0 //defaults to 1.0x zoom

    override fun setParent(parent: UserWrapper) {
        user = parent
    }

}