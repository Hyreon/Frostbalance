package botmanager.frostbalance.menu

import botmanager.frostbalance.Frostbalance
import net.dv8tion.jda.api.EmbedBuilder
import botmanager.frostbalance.Nation
import java.awt.Color

class AllegianceMenu @JvmOverloads constructor(bot: Frostbalance, var cause: Cause = Cause.NOT_SET) : Menu(bot) {
    override fun getMEBuilder(): EmbedBuilder {
        val builder = EmbedBuilder()
        if (isClosed) {
            builder.setTitle("Allegiance set")
            if (cause == Cause.NOT_SET) {
                builder.setDescription("You will now claim tiles in the name of " + getActor().playerIn(bot.mainNetwork).allegiance + ". You may now make claims.")
            } else if (cause == Cause.CHANGE) {
                builder.setDescription("Your allegiance has been moved to " + getActor().playerIn(bot.mainNetwork).allegiance)
            }
            builder.setColor(getActor().playerIn(bot.mainNetwork).allegiance?.color ?: Color.GRAY)
        } else {
            builder.setTitle("Set allegiance")
            if (cause == Cause.NOT_SET) {
                builder.setDescription("This claim cannot be made. In order to claim tiles, you must first set your allegiance." + ADDENDUM)
                builder.setColor(Color.GRAY)
            } else if (cause == Cause.CHANGE) {
                builder.setDescription("Pick your new allegiance. Current allegiance: " + getActor().playerIn(bot.mainNetwork).allegiance + ADDENDUM)
                builder.setColor(getActor().playerIn(bot.mainNetwork).allegiance?.color ?: Color.GRAY)
            }
        }
        return builder
    }

    enum class Cause {
        NOT_SET, CHANGE
    }

    companion object {
        private const val ADDENDUM = "\n*Note: If you change your allegiance, your claims will cease to be valid until you return to your original nation. You **cannot** move claims from one nation to another!*"
    }

    init {
        for (nation in Nation.nations) {
            menuResponses.add(object : MenuResponse(nation.emoji, nation.toString()) {
                override fun reactEvent() {
                    getActor().playerIn(bot.mainNetwork).allegiance = nation
                    close(false)
                }

                override fun validConditions(): Boolean {
                    return true
                }
            })
        }
        menuResponses.add(object : MenuResponse("✖️", "Don't change for now") {
            override fun reactEvent() {
                close(true)
            }

            override fun validConditions(): Boolean {
                return true
            }
        })
    }
}