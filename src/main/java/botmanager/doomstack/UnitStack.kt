package botmanager.doomstack

import java.lang.Math.floor
import kotlin.math.roundToInt

class UnitStack (
    val type: CombatUnitClass = CombatUnitClass(),
    val rank: CombatUnit.Rank = CombatUnit.Rank.REGULAR,
    budget: Double = 60.0
) {

    val count: Int = ((budget / type.cost) * (3.0 / rank.hp)).roundToInt()

    override fun toString(): String {
        return arrayOf(
            "$count*${rank.char}* ${type.name}",
        ).joinToString()
    }

}
