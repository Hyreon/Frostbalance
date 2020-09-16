package botmanager.frostbalance.flags

import botmanager.frostbalance.GuildWrapper
import botmanager.frostbalance.UserWrapper
import botmanager.frostbalance.grid.Containable
import kotlin.properties.ReadWriteProperty

class GuildOptions(@Transient var guild: GuildWrapper): Containable<GuildWrapper> {

    var openBorders: Boolean? = null
        get() = field ?: true //defaults to TRUE

    override fun setParent(parent: GuildWrapper) {
        guild = parent
    }

}
