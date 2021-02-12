package botmanager.frostbalance.command

import botmanager.frostbalance.Influence
import botmanager.frostbalance.grid.coordinate.Hex
import botmanager.frostbalance.grid.coordinate.HexDomain

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
        return next()?.let { next -> lastArgument = next
            return try {
                Influence(next).takeIf { it > 0}
            } catch (e: NumberFormatException) {
                null
            }
        }
    }

    fun nextInteger(): Int? {
        return next()?.let { next -> lastArgument = next
            return try {
                next.toInt()
            } catch (e: java.lang.NumberFormatException) {
                null
            }
        }
    }

    fun nextHexDomain(): HexDomain? {
        return next()?.let { HexDomain(it) }
    }

    fun nextCoordinate(): Hex? {
        if (arguments.getOrNull(0)?.startsWith(COORDINATE_PRE_DELIMITER) == true) {

            val coordinates: MutableList<Long> = mutableListOf()
            val savedSubArguments: MutableList<String> = mutableListOf()

            val lastArgument = arguments.first { it.endsWith(COORDINATE_POST_DELIMITER) }
            for (argument : String in arguments) {
                var effectiveArgument = argument
                if (argument == arguments[0]) effectiveArgument = argument.removePrefix(COORDINATE_PRE_DELIMITER)
                if (effectiveArgument.endsWith(COORDINATE_MID_DELIMITER)) {
                    effectiveArgument = effectiveArgument.removeSuffix(COORDINATE_MID_DELIMITER)
                }
                if (effectiveArgument == lastArgument) effectiveArgument = effectiveArgument.removeSuffix(COORDINATE_POST_DELIMITER)
                if (effectiveArgument.contains(COORDINATE_MID_DELIMITER)) {
                    savedSubArguments.addAll(effectiveArgument.split(COORDINATE_MID_DELIMITER))
                }

                if (savedSubArguments.size == 0) {
                    coordinates.add(effectiveArgument.toLong())
                } else {
                    while (savedSubArguments.size > 0) {
                        coordinates.add(savedSubArguments.removeAt(0).toLong())
                    }
                }
                if (argument == lastArgument) break //nothing after this
            }

            if (coordinates.size == 2) {
                while (arguments[0] != lastArgument) arguments.removeFirst()
                arguments.removeFirst()
                return Hex(coordinates[0], coordinates[1])
            } else if (coordinates.size == 3) {
                while (arguments[0] != lastArgument) arguments.removeFirst()
                arguments.removeFirst()
                return Hex(coordinates[0], coordinates[1], coordinates[2])
            } else return null

        } else return null
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
        return try {
            Hex(x.toLong(), y.toLong(), z.toLong())
        } catch (e: NumberFormatException) {
            null
        }
    }

    companion object {
        const val COORDINATE_PRE_DELIMITER = "("
        val COORDINATE_MID_DELIMITER = ","
        const val COORDINATE_POST_DELIMITER = ")"
    }

}