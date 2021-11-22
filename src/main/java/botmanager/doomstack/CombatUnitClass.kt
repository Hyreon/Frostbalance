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

        val MOBILE_SAM = CombatUnitClass("Mobile SAM", 100, 1, 6)
        val TOW_INFANTRY = CombatUnitClass("TOW Infantry", 120, 12, 14, moves = 3)
        val MECH_INFANTRY = CombatUnitClass("Mech Infantry", 110, 12, 18)
        val MODERN_ARMOR = CombatUnitClass("Modern Armor", 120, 24, 16)
        val MODERN_PARATROOPER = CombatUnitClass("Modern Paratrooper", 110, 6, 11)
        //val RADAR_ARTILLERY = CombatUnitClass("Radar Artillery", 120, 0, 0, bombard = 16, fireRate = 3)

        //note: attack and defense are actually 1:1.
        val CHASQUI_SCOUT = CombatUnitClass("Chasqui Scout", 20, 1000, 1000, moves = 2)

        val ENKIDU_WARRIOR = CombatUnitClass("Enkidu Warrior", 10, 1, 2)
        val JAGUAR_WARRIOR = CombatUnitClass("Jaguar Warrior", 15, 1, 1, moves = 2)
        val BOWMAN = CombatUnitClass("Bowman", 20, 2, 2, bombard = 1)
        val JAVELIN_THROWER = CombatUnitClass("Javelin Thrower", 30, 2, 2)
        val IMPI = CombatUnitClass("Impi", 20, 1, 2, moves = 2)
        val HOPLITE = CombatUnitClass("Hoplite", 20, 1, 3)
        val NUMIDIAN_MERCENARY = CombatUnitClass("Numidian Mercenary", 30, 2, 3)
        val LEGIONARY = CombatUnitClass("Legionary", 30, 3, 3)
        val IMMORTALS = CombatUnitClass("Immortals", 30, 4, 2)
        val GALLIC_SWORDSMAN = CombatUnitClass("Gallic Swordsman", 40, 3, 2, moves = 2)
        val WAR_CHARIOT = CombatUnitClass("War Chariot", 20, 2, 1, moves = 2)
        val THREE_MAN_CHARIOT = CombatUnitClass("Three Man Chariot", 30, 2, 2, moves = 2)
        val MOUNTED_WARRIOR = CombatUnitClass("Mounted Warrior", 30, 3, 1, moves = 2)

        val SWISS_MERCENARY = CombatUnitClass("Swiss Mercenary", 40, 1, 4)
        val BERSERK = CombatUnitClass("Berserk", 70, 6, 2)
        val RIDER = CombatUnitClass("Rider", 70, 4, 3, moves = 3)
        val SAMURAI = CombatUnitClass("Samurai", 70, 4, 4, moves = 2)
        val KESHIK = CombatUnitClass("Keshik", 60, 4, 2, moves = 2)
        val ANSAR_WARRIOR = CombatUnitClass("Ansar Warrior", 60, 4, 2, moves = 3)
        val WAR_ELEPHANT = CombatUnitClass("War Elephant", 70, 4, 3, moves = 2, bonus = 1)
        val MUSKETEER = CombatUnitClass("Musketeer", 60, 2, 5, bombard = 2)
        val CONQUISTADOR = CombatUnitClass("Conquistador", 70, 3, 2, moves = 2)
        //val HWACHA = CombatUnitClass("Hwach'a", 40, 0, 0, bombard = 8, fireRate = 1, lethal = true)

        val COSSACK = CombatUnitClass("Cossack", 90, 6, 3, moves = 3, blitz = true)
        val SIPAHI = CombatUnitClass("Sipahi", 90, 8, 3, moves = 3)
        val PANZER = CombatUnitClass("Panzer", 100, 16, 8, moves = 3, blitz = true)


        val ancientPool = listOf(WARRIOR, ARCHER, SPEARMAN, SWORDSMAN, CHARIOT, HORSEMAN)
        val medievalPool = listOf(PIKEMAN, MEDIEVAL_INFANTRY, LONGBOWMAN, MUSKETMAN, KNIGHT)
        val industrialPool = listOf(CAVALRY, RIFLEMAN, GUERILLA, INFANTRY, TANK, FLAK, PARATROOPER, MARINE)
        val modernPool = listOf(MOBILE_SAM, TOW_INFANTRY, MECH_INFANTRY, MODERN_ARMOR, MODERN_PARATROOPER)
        val ancientPoolNat = listOf(ENKIDU_WARRIOR, JAGUAR_WARRIOR, BOWMAN, JAVELIN_THROWER, IMPI, HOPLITE, ANCIENT_CAVALRY,
            NUMIDIAN_MERCENARY, LEGIONARY, IMMORTALS, GALLIC_SWORDSMAN, WAR_CHARIOT, THREE_MAN_CHARIOT, MOUNTED_WARRIOR)
        val medievalPoolNat = listOf(SWISS_MERCENARY, BERSERK, RIDER, SAMURAI, KESHIK, ANSAR_WARRIOR, WAR_ELEPHANT,
            MUSKETEER, CONQUISTADOR, CRUSADER)
        val industrialPoolNat = listOf(COSSACK, SIPAHI, PANZER)
        val uniquePool = arrayListOf(ancientPool, medievalPool, ancientPoolNat, medievalPoolNat).flatten()
        val totalPool = arrayListOf(ancientPool, medievalPool, industrialPool, modernPool).flatten()

    }
}