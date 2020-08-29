package botmanager.frostbalance.command

import botmanager.frostbalance.Influence
import botmanager.frostbalance.grid.coordinate.Hex
import botmanager.frostbalance.grid.coordinate.HexDomain
import java.util.*

class ArgumentStream(var arguments: MutableList<String>) {

    constructor(params: Array<out String>) : this(params.toMutableList())

    fun next(): String? {
        return arguments.removeFirstOrNull()
    }

    fun exhaustArguments(amountToSpare: Int = 0): String {
        val subList = arguments.subList(0, (arguments.size - amountToSpare).coerceAtLeast(0))
        val longString = subList.joinToString(" ")
        arguments.removeAll ( subList )
        return longString
    }

    fun nextInfluence(): Influence? {
        return next()?.let { Influence(it) }
    }

    fun nextHexDomain(): HexDomain? {
        return next()?.let { HexDomain(it) }
    }

    /**
     * Exhausts the next three values, treating them as coordinates.
     * If there aren't three values, it will return null instead.
     */
    fun nextSpacedCoordinate(): Hex? {
        val x: String? = next()
        val y: String? = next()
        val z: String? = next()
        return if (x != null && y != null && z != null) {
            Hex(x.toLong(), y.toLong(), z.toLong())
        } else {
            null
        }
    }

}