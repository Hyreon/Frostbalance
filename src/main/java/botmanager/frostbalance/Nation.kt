package botmanager.frostbalance

import java.awt.Color

/**
 * A simple flag to determine which nation something is a part of.
 * Main guilds have the three nations; all other guilds have the NONE nation.
 * The NONE nation can also be used in some contexts for the main nations.
 */
enum class Nation(var emoji: String, var color: Color) {

    RED("\uD83D\uDFE5", Color(0xff, 0, 0)),
    GREEN("\uD83D\uDFE9", Color(0, 0xbb, 0)),
    BLUE("\uD83D\uDFE6", Color(0, 0x44, 0xff)),

    PURPLE("\uD83D\uDFEA", Color(0x80, 0, 0xff)),
    YELLOW("\uD83D\uDFE8", Color(0xff, 0xff, 0)),
    ORANGE("\uD83D\uDFE7", Color(0xff, 0x80, 0)),
    DARK("⬛", Color(0x12, 0x12, 0x12)),
    LIGHT("⬜", Color(0xcc, 0xcc, 0xcc));

    override fun toString(): String {
        return name.toLowerCase().capitalize()
    }

    @Deprecated("Assumes this is on the main map")
    val effectiveName: String
        get() {
            val guild = Frostbalance.bot.getGuildFor(this)
            return guild?.name ?: this.toString()
        }
}