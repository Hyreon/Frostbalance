package botmanager.frostbalance.menu.settings

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.Nation
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.GuildMessageContext
import botmanager.frostbalance.menu.Menu
import botmanager.frostbalance.menu.input.BooleanMenu
import botmanager.frostbalance.menu.option.NationOptionMenu
import botmanager.frostbalance.menu.response.DynamicMenuResponse
import botmanager.frostbalance.menu.response.MenuResponse
import net.dv8tion.jda.api.EmbedBuilder

class NationSettingsMenu(bot: Frostbalance, context: GuildMessageContext) : Menu(bot, context) {

    init {
        menuResponses.add(object : MenuResponse(context.guild.nation.emoji, "Change nation") {

            override val isValid: Boolean
                get() = originalMenu.actor?.memberIn(context.guild)?.hasAuthority(AuthorityLevel.MAP_ADMIN) ?: false

            override fun reactEvent() {

                val availableNations: Set<Nation> = Nation.values().subtract(context.gameNetwork.nations)

                redirectTo(object : NationOptionMenu(bot, context, availableNations.toList()) {

                    override fun select(selectedNation: Nation) {
                        context.guild.nation = selectedNation
                        emoji = selectedNation.emoji
                        close(false)
                    }

                    override val embedBuilder: EmbedBuilder
                        get() = super.embedBuilder.setDescription(":warning: This will cause compatibility issues with maps and users " +
                                "if they have already set their allegiance to this nation. Changing back to the original nation should, but " +
                                "is not guaranteed, to fix any issues.")

                }, true)

            }

        })

        menuResponses.add(object : DynamicMenuResponse({"\uD83D\uDEC2"}, {"Open Borders: ${context.guild.guildOptions.openBorders}"}) {
            override fun reactEvent() {

                redirectTo(BooleanMenu(bot, context, {
                    context.guild.guildOptions.openBorders = true
                }, {
                    context.guild.guildOptions.openBorders = false
                }, "$emoji $name"), true)

            }

            override val isValid: Boolean
                get() = true

        })
    }

    override val embedBuilder: EmbedBuilder
        get() {
            for (response in menuResponses) {
                if (response is DynamicMenuResponse) {
                    response.updateValues()
                }
            }
            return EmbedBuilder()
                    .setTitle("Nation Settings")
        }
}