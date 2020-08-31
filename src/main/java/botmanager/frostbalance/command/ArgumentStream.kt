package botmanager.frostbalance.command

import botmanager.frostbalance.Influence
import botmanager.frostbalance.grid.coordinate.Hex
import botmanager.frostbalance.grid.coordinate.HexDomain
import java.util.*

class ArgumentStream(var arguments: MutableList<String>) {

    /**
     * Returns the last argument that was exhausted.
     */
    var lastArgument: String? = null

    constructor(params: Array<out String>) : this(params.toMutableList())

    fun next(): String? {
        lastArgument = arguments.removeFirstOrNull()
        return lastArgument
    }

    fun nextFor(amount: Int): String {
        val subList = arguments.subList(0, amount.coerceAtMost(arguments.size))
        val longString = subList.joinToString(" ")
        arguments.removeAll ( subList )
        lastArgument = longString
        return longString
    }

    fun exhaust(amountToSpare: Int = 0): String {
        val subList = arguments.subList(0, (arguments.size - amountToSpare).coerceAtLeast(0))
        val longString = subList.joinToString(" ")
        arguments.removeAll ( subList )
        lastArgument = longString
        return longString
    }

    /**
     * @param positive Whether the influence has to be positive or not
     */
    fun nextInfluence(positive: Boolean = false): Influence? {
        return next()?.let { next -> lastArgument = next; Influence(next).takeIf { it > 0} }
    }

    fun nextHexDomain(): HexDomain? {
        return next()?.let { HexDomain(it) }
    }

    fun nextCoordinate(): Hex? {
        return nextSpacedCoordinate()
    }

    /**
     * Exhausts the next three values, treating them as coordinates.
     * If there aren't three values, it will return null instead.
     */
    fun nextSpacedCoordinate(): Hex? {
        val coords = nextFor(3).split(" ")
        val x: String = coords.getOrNull(0) ?: return null
        val y: String = coords.getOrNull(1)  ?: return null
        val z: String = coords.getOrNull(2)  ?: return null
        return Hex(x.toLong(), y.toLong(), z.toLong())
    }

}