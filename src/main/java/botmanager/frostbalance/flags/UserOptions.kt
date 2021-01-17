package botmanager.frostbalance.flags

import botmanager.frostbalance.UserWrapper
import botmanager.frostbalance.grid.Containable
import botmanager.frostbalance.grid.coordinate.Hex

class UserOptions(@Transient var user: UserWrapper) : Containable<UserWrapper> {

    var drawCoords: Boolean? = null
        get() = field ?: false

    var coordSys: Hex.CoordSys? = null
        get() = field ?: Hex.CoordSys.NAVIGATOR

    var zoomSize: Double? = null
        get() = field ?: 1.0 //defaults to 1.0x zoom

    var loopActions: Boolean? = null
        get() = field ?: false

    override fun setParent(parent: UserWrapper) {
        user = parent
    }

}