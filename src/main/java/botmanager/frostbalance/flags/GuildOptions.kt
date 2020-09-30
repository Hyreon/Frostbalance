package botmanager.frostbalance.flags

import botmanager.frostbalance.GuildWrapper
import botmanager.frostbalance.Nation
import botmanager.frostbalance.UserWrapper
import botmanager.frostbalance.grid.Containable
import java.util.*
import kotlin.collections.HashMap
import kotlin.properties.ReadWriteProperty

class GuildOptions(@Transient var guild: GuildWrapper): Containable<GuildWrapper> {

    var openBorders: Boolean? = null
        get() = field ?: true //defaults to TRUE

    var borderTreaties: Set<Nation>? = null
        get() = field ?: HashSet()

    fun openBordersWith(nation: Nation?): Boolean {
        return openBorders!! || borderTreaties!!.contains(nation)
    }

    override fun setParent(parent: GuildWrapper) {
        guild = parent
    }

    fun flipTreatyWith(option: Nation) {
        borderTreaties = if (borderTreaties!!.contains(option)) {
            borderTreaties!!.minus(option)
        } else {
            borderTreaties!!.plus(option)
        }
    }

}
