package botmanager.doomstack

data class CombatUnitClass(

    // META STATS
    val name: String = "Warrior",
    val cost: Int = 10,

    // NORMAL STATS
    val attack: Int = 1,
    val defense: Int = 1,

    // BONUSES
    val bonus: Int = 0,
    val moves: Int = 1,
    val blitz: Boolean = false,
    val bombard: Int = 0,
    val fireRate: Int = 0
) {
    companion object {

        val WARRIOR = CombatUnitClass() //default
        val ARCHER = CombatUnitClass("Archer", 20, 2, 1, bombard = 1)
        val SPEARMAN = CombatUnitClass("Spearman", 20, 1, 2)
        val SWORDSMAN = CombatUnitClass("Swordsman", 30, 3, 2)
        val CHARIOT = CombatUnitClass("Chariot", 20, moves = 2)
        val HORSEMAN = CombatUnitClass("Horseman", 30, 2, 1, moves = 2)
        //val CATAPULT = CombatUnitClass("Catapult", 20, 0, 0, bombard = 4, fireRate = 1)
        val ANCIENT_CAVALRY = CombatUnitClass("Ancient Cavalry", 40, 3, 2, moves = 2, bonus = 1)

        val PIKEMAN = CombatUnitClass("Pikeman", 30, 1, 3)
        val MEDIEVAL_INFANTRY = CombatUnitClass("Medieval Infantry", 40, 4, 2)
        val LONGBOWMAN = CombatUnitClass("Longbowman", 40, 4, 1, bombard = 2)
        val MUSKETMAN = CombatUnitClass("Musketman", 60, 2, 4)
        val KNIGHT = CombatUnitClass("Knight", 70, 4, 3, moves = 2)
        //val TREBUCHET = CombatUnitClass("Trebuchet", 30, 0, 0, bombard = 6, fireRate = 1)
        //val CANNON = CombatUnitClass("Cannon", 40, 0, 0, bombard = 8, fireRate = 1)
        val CRUSADER = CombatUnitClass("Crusader", 70, 5, 3)

        val CAVALRY = CombatUnitClass("Cavalry", 80, 6, 3, moves = 3)
        val RIFLEMAN = CombatUnitClass("Rifleman", 80, 4, 6)
        val GUERILLA = CombatUnitClass("Guerilla", 90, 6, 6)
        val INFANTRY = CombatUnitClass("Infantry", 90, 6, 10)
        val TANK = CombatUnitClass("Tank", 100, 16, 8, moves = 2, blitz = true)
        val FLAK = CombatUnitClass("Flak", 70, 1, 6)
        val PARATROOPER = CombatUnitClass("Paratrooper", 90, 4, 9)
        val MARINE = CombatUnitClass("Marine", 120, 12, 6)
        //val ARTILLERY = CombatUnitClass("Artillery", 80, 0, 0, bombard = 12, fireRate = 2)

        val TOW_INFANTRY = CombatUnitClass("TOW Infantry", 120, 12, 14, moves = 3)
        val MECH_INFANTRY = CombatUnitClass("Mech Infantry", 110, 12, 18)
        val MODERN_ARMOR = CombatUnitClass("Modern Armor", 120, 24, 16)
        val MODERN_PARATROOPER = CombatUnitClass("Modern Paratrooper", 110, 6, 11)
        //val RADAR_ARTILLERY = CombatUnitClass("Radar Artillery", 120, 0, 0, bombard = 16, fireRate = 3)

        val ancientPool = listOf(WARRIOR, ARCHER, SPEARMAN, SWORDSMAN, CHARIOT, HORSEMAN, ANCIENT_CAVALRY)
        val medievalPool = listOf(PIKEMAN, MEDIEVAL_INFANTRY, LONGBOWMAN, MUSKETMAN, KNIGHT, CRUSADER)
        val industrialPool = listOf(CAVALRY, RIFLEMAN, GUERILLA, INFANTRY, TANK, FLAK, PARATROOPER, MARINE)
        val modernPool = listOf(TOW_INFANTRY, MECH_INFANTRY, MODERN_ARMOR, MODERN_PARATROOPER)
        val totalPool = arrayListOf(ancientPool, medievalPool, industrialPool, modernPool).flatten()

    }
}