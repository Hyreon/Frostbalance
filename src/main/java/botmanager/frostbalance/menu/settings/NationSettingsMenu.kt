package botmanager.frostbalance.menu.settings

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.Nation
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.CommandContext
import botmanager.frostbalance.command.GuildCommandContext
import botmanager.frostbalance.menu.Menu
import botmanager.frostbalance.menu.option.NationOptionMenu
import botmanager.frostbalance.menu.option.OptionMenu
import botmanager.frostbalance.menu.response.MenuResponse
import net.dv8tion.jda.api.EmbedBuilder
import java.util.*
import kotlin.collections.HashSet

class NationSettingsMenu(bot: Frostbalance, context: GuildCommandContext) : Menu(bot, context) {
    override val embedBuilder: EmbedBuilder
        get() = EmbedBuilder()
                .setTitle("Nation Settings")

    init {
        menuResponses.add(object : MenuResponse(context.guild.nation.emoji, "Change nation") {

            override val isValid: Boolean
                get() = originalMenu.actor?.memberIn(context.guild)?.hasAuthority(AuthorityLevel.MAP_ADMIN) ?: false

            override fun reactEvent() {

                val availableNations: Set<Nation> = Nation.values().subtract(context.gameNetwork.nations)

                adopt(object : NationOptionMenu(bot, context, availableNations.toList()) {

                    override fun select(selectedNation: Nation) {
                        context.guild.nation = selectedNation
                        emoji = selectedNation.emoji
                        close(false)
                    }

                }, true)

            }

        })
    }
}