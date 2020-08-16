package botmanager.frostbalance

import java.awt.Color

/**
 * A simple flag to determine which nation something is a part of.
 * Main guilds have the three nations; all other guilds have the NONE nation.
 * The NONE nation can also be used in some contexts for the main nations.
 */
enum class NationColor(var color: Color) {
    RED(Color.RED), GREEN(Color.GREEN), BLUE(Color.BLUE), NONE(Color.LIGHT_GRAY);

    fun adjustDisplayColor(color: Color, drawValue: Int): Color {
        return when (this) {
            RED -> Color(drawValue, color.green, color.blue)
            GREEN -> Color(color.red, drawValue, color.blue)
            BLUE -> Color(color.red, color.green, drawValue)
            else -> color
        }
    }

    val effectiveName: String
        get() {
            val guild = Frostbalance.bot.getGuildFor(this)
            return guild?.name ?: this.toString()
        }

    companion object {
        /**
         * Returns all nations without NONE
         * @return All nations without NONE
         */
        @JvmStatic
        val nationColors: Array<NationColor>
            get() = arrayOf(RED, GREEN, BLUE)
    }
}