package botmanager.doomstack.menu

import botmanager.doomstack.*
import net.dv8tion.jda.api.EmbedBuilder

class FightMenu(bot: DoomStack, context: MessageContext, val attackers: List<UnitStack>, val defenders: List<UnitStack>) : Menu(bot, context) {

    val attackQueue: MutableList<CombatUnit> = mutableListOf()
    val defenseQueue: MutableList<CombatUnit> = mutableListOf()

    val log: MutableList<String> = mutableListOf()

    init {
        attackers.forEach { attackerType ->
            for (i in 0 until attackerType.count) {
                attackQueue.add(CombatUnit(attackerType.type, attackerType.rank))
            }
        }
        defenders.forEach { defenderType ->
            for (i in 0 until defenderType.count) {
                defenseQueue.add(CombatUnit(defenderType.type, defenderType.rank))
            }
        }
    }

    var turn = 0

    fun doTurn() {
        log.add("**Turn $turn**")
        if (turn != 0) {
            dumbMoves(attackQueue, defenseQueue)
            log.add("*Defender's turn!*")
        } else { //attackers' consolidation turn, only relevant for cavalry and modern armor
            advance(attackQueue)
            dumbMoves(attackQueue, defenseQueue)
            log.add("*Attackers advance, defenders move first!*")
        }
        dumbMoves(defenseQueue, attackQueue)
        turn++
        if (defenseQueue.isEmpty()) {
            close(false)
        } else if (attackQueue.isEmpty()) {
            close(false)
        } else {
            for (unit in attackQueue) {
                unit.reset(!unit.hasAttacked)
            }
            for (unit in defenseQueue) {
                unit.reset(!unit.hasAttacked)
            }
            doTurn()
        }
    }

    private fun advance(queue: MutableList<CombatUnit>) {
        for (unit in queue) {
            unit.movesTaken += 1
        }
    }

    /**
     * A very simple AI that attacks when uninjured, regardless of the attacker or defender.
     */
    private fun dumbMoves(attackers: MutableList<CombatUnit>, defenders: MutableList<CombatUnit>) {
        val attackerCount = attackers.size
        for (i in 0 until attackerCount) {
            val unit = attackers.removeFirst()
            if (unit.HP != unit.maxHP) {
                unit.skip()
                attackers.add(unit)
                log.add("${unit.stats.name} (${unit.HP}/${unit.maxHP}) fortifies")
            } else {
                if (defenders.isEmpty()) {
                    attackers.add(unit)
                    return
                }
                defenders.sortDescending()
                println(defenders.map { defender -> defender.HP })
                val outcome = unit.attack(defenders.first(), null, 0.1)
                when {
                    outcome.outcome == CombatLog.BattleOutcome.VICTORY -> {
                        val baseText = "${unit.stats.name} (${unit.HP}/${unit.maxHP} -${outcome.damageTaken}) attacks ${defenders.first().stats.name} for -${outcome.damageDealt} and is victorious"
                        if (outcome.promotion) {
                            log.add(baseText + ", promoting to *${unit.rank}!*")
                        } else {
                            log.add(baseText)
                        }
                        defenders.removeFirst()
                        attackers.add(unit) // regroup
                    }
                    outcome.outcome == CombatLog.BattleOutcome.RETREAT -> {
                        attackers.add(unit) // regroup
                        val baseText = "${unit.stats.name} (${unit.HP}/${unit.maxHP} -${outcome.damageTaken}) regroups after retreating from ${defenders.first().stats.name} (${defenders.first().HP}/${defenders.first().maxHP} -${outcome.damageDealt})"
                        log.add(baseText)
                    }
                    outcome.outcome == CombatLog.BattleOutcome.BUGGED -> {
                        attackers.add(unit) // regroup
                        val baseText = "${unit.stats.name} regroups after bugged move"
                        log.add(baseText)
                    }
                    outcome.outcome == CombatLog.BattleOutcome.DEFEAT -> {
                        val baseText = "${unit.stats.name} attacks ${defenders.first().stats.name} (${defenders.first().HP}/${defenders.first().maxHP} -${outcome.damageDealt}) and is defeated for -${outcome.damageTaken}"
                        if (outcome.promotion) {
                            log.add(baseText + ", promoting it to *${defenders.first().rank}!*")
                        } else {
                            log.add(baseText)
                        }
                    }
                    else -> {
                        attackers.add(unit) //regroup after illegal attack
                    }
                }
                if (defenders.isEmpty()) return
            }
        }
    }

    override val embedBuilder: EmbedBuilder
        get() {
            val builder = EmbedBuilder()
            if (attackQueue.isNotEmpty() && defenseQueue.isNotEmpty()) {
                builder.setTitle("DOOM STACK IN PROGRESS!!")
                builder.setDescription("...it'll be a minute.")
            } else if (attackQueue.isEmpty()) {
                builder.setTitle("DEFENDERS WIN!!")
                builder.setDescription(log.joinToString("\n").takeLast(2000))
            } else if (defenseQueue.isEmpty()) {
                builder.setTitle("ATTACKERS WIN!!")
                builder.setDescription(log.joinToString("\n").takeLast(2000))
            }
            return builder
        }

}
