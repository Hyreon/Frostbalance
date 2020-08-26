package botmanager.frostbalance.menu

import botmanager.frostbalance.Frostbalance
import net.dv8tion.jda.api.EmbedBuilder
import botmanager.frostbalance.Nation
import botmanager.frostbalance.command.CommandContext
import botmanager.frostbalance.menu.response.MenuResponse
import java.awt.Color

class AllegianceMenu @JvmOverloads constructor(bot: Frostbalance, context: CommandContext, var cause: Cause = Cause.NOT_SET) : Menu(bot, context) {
    override val embedBuilder: EmbedBuilder
    get() = {
        assert(actor != null)
        val builder = EmbedBuilder()
        if (isClosed) {
            builder.setTitle("Allegiance set")
            if (cause == Cause.NOT_SET) {
                builder.setDescription("You will now claim tiles in the name of " + actor!!.playerIn(bot.mainNetwork).allegiance + ". You may now make claims.")
            } else if (cause == Cause.CHANGE) {
                builder.setDescription("Your allegiance has been moved to " + actor!!.playerIn(bot.mainNetwork).allegiance)
            }
            builder.setColor(actor!!.playerIn(bot.mainNetwork).allegiance?.color ?: Color.GRAY)
        } else {
            builder.setTitle("Set allegiance")
            if (cause == Cause.NOT_SET) {
                builder.setDescription("In order to do this, you must first set your allegiance.$ADDENDUM")
                builder.setColor(Color.GRAY)
            } else if (cause == Cause.CHANGE) {
                builder.setDescription("Pick your new allegiance. Current allegiance: " + actor!!.playerIn(bot.mainNetwork).allegiance + ADDENDUM)
                builder.setColor(actor!!.playerIn(bot.mainNetwork).allegiance?.color ?: Color.GRAY)
            }
        }
        builder
    }.invoke()

    enum class Cause {
        NOT_SET, CHANGE
    }

    companion object {
        private const val ADDENDUM = "\n*Note: You can change your allegiance at any time. However, if you change your allegiance, your claims will cease to be valid until you return to your original nation. You **cannot** move claims from one nation to another!*"
    }

    init {
        for (nation in context.gameNetwork.nations) {
            menuResponses.add(object : MenuResponse(nation.emoji, nation.toString()) {
                override fun reactEvent() {
                    actor!!.playerIn(bot.mainNetwork).allegiance = nation
                    close(false)
                }

                override val isValid: Boolean
                    get() = true
            })
        }
        menuResponses.add(object : MenuResponse("✖️", "Don't set for now") {
            override fun reactEvent() {
                close(true)
            }

            override val isValid: Boolean
                get() = true
        })
    }
}