package botmanager.doomstack

class CombatLog(val outcome: BattleOutcome, val damageTaken: Int = 0, val damageDealt: Int = 0, val promotion: Boolean = false) {

    enum class BattleOutcome {
        VICTORY,
        DEFEAT,
        RETREAT,
        ILLEGAL_EXHAUSTED,
        ILLEGAL_NO_DOUBLES,
        BUGGED
    }


}