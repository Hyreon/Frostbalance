package botmanager.doomstack

import kotlin.random.Random

class CombatUnit(val stats: CombatUnitClass, var rank: Rank = Rank.REGULAR): Comparable<CombatUnit> {

    // CURRENT STATS
    var damage = 0
    var movesTaken = 0
    var hasAttacked = false
    var isFortified = false
    var hasBombarded = false

    val maxHP
        get() = rank.hp + stats.bonus
    val HP
        get() = maxHP - damage
    val movesLeft
        get() = stats.moves - movesTaken

    enum class Rank(val hp: Int, val char: Char, val retreat: Int) {
        CONSCRIPT(2, 'C', 34),
        REGULAR(3, 'R', 50),
        VETERAN(4, 'V', 58),
        ELITE(5, 'E', 66)
    }

    //TODO get real probabilities
    fun tryPromote(): Boolean {
        return if (!Random.nextBoolean()) false
        else {
            when (rank) {
                Rank.CONSCRIPT -> {rank = Rank.REGULAR; true}
                Rank.REGULAR -> {rank = Rank.VETERAN; true}
                Rank.VETERAN -> {rank = Rank.ELITE; true}
                else -> false
            }
        }
    }

    fun effectiveDefense(terrain: Double): Double {
        var mult = 1.0
        if (isFortified) mult += 0.25
        mult += terrain

        return mult * stats.defense
    }

    fun reset(healing: Boolean) {
        if (healing) {
            damage--
            if (damage < 0) damage = 0
        }

        movesTaken = 0
        hasAttacked = false
        hasBombarded = false
    }

    fun skip() {
        if (movesLeft == 0) return //nothing to do here
        isFortified = true
        movesTaken = stats.moves
    }

    fun attack(other: CombatUnit, artillery: CombatUnit?, terrain: Double): CombatLog {
        if (movesLeft == 0) return CombatLog(CombatLog.BattleOutcome.ILLEGAL_EXHAUSTED)
        else if (!stats.blitz && hasAttacked) return CombatLog(CombatLog.BattleOutcome.ILLEGAL_NO_DOUBLES)
        else if (HP > 1) { //artillery check
            val bStrength = artillery?.stats?.bombard ?: 0
            val bombardOutcome = Random.nextDouble()
            //TODO check if defensive bombard is affected by terrain, and if so what terrain
            if (bombardOutcome < bStrength / (bStrength + other.stats.defense) ) {
                damage++
                artillery?.hasBombarded = true
            }
        } //actual attack
        isFortified = false
        hasAttacked = true
        movesTaken++
        return doRounds(other, terrain)
    }

    //TODO add promotions
    fun doRounds(other: CombatUnit, terrain: Double): CombatLog {

        var damageTaken = 0
        var damageDealt = 0

        while (HP > 0 && other.HP > 0) {
            val outcome = Random.nextDouble()
            if (outcome < stats.attack / (stats.attack + other.effectiveDefense(terrain)) ) { //win a round
                other.damage++
                damageDealt++
            } else { //lose a round
                this.damage++
                damageTaken++

                if (this.HP == 1 && other.HP != 1 && stats.moves > 1 && other.stats.moves == 1) { //attempt retreat!

                    val retreatOutcome = Random.nextDouble()
                    if (retreatOutcome < rank.retreat/(other.rank.retreat+50.0)) {

                        return CombatLog(CombatLog.BattleOutcome.RETREAT) // retreat successful

                    }

                }
            }

        }

        if (HP == 0) {
            val enemyPromoteOutcome = other.tryPromote()
            return CombatLog(CombatLog.BattleOutcome.DEFEAT, damageTaken, damageDealt, enemyPromoteOutcome)
        }

        if (other.HP == 0) {
            val promoteOutcome = tryPromote()
            return CombatLog(CombatLog.BattleOutcome.VICTORY, damageTaken, damageDealt, promoteOutcome)
        }

        return CombatLog(CombatLog.BattleOutcome.BUGGED, damageTaken, damageDealt, false)

    }

    //TODO make this compareTo use the actual terrain value, not just flat all the time
    override fun compareTo(other: CombatUnit): Int {
        val defenseProduct = effectiveDefense(0.1) * HP
        val otherDefenseProduct = other.effectiveDefense(0.1) * other.HP
        return when {
            defenseProduct > otherDefenseProduct -> {
                1
            }
            defenseProduct < otherDefenseProduct -> {
                -1
            }
            else -> {
                when {
                    other.maxHP > maxHP -> {
                        1
                    }
                    other.maxHP == maxHP -> {
                        0
                    }
                    else -> {
                        -1
                    }
                }
            }
        }
    }

}