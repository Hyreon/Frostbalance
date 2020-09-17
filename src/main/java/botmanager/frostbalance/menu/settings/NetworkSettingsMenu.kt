package botmanager.frostbalance.menu.settings

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.GameNetwork
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.GuildMessageContext
import botmanager.frostbalance.command.MessageContext
import botmanager.frostbalance.menu.Menu
import botmanager.frostbalance.menu.option.OptionMenu
import botmanager.frostbalance.menu.response.MenuResponse
import botmanager.frostbalance.menu.response.SimpleTextHook
import net.dv8tion.jda.api.EmbedBuilder

class NetworkSettingsMenu(bot: Frostbalance, context: GuildMessageContext) : Menu(bot, context) {

    init {

        menuResponses.add(object : MenuResponse("📝", "Network name") {

            override val isValid: Boolean
                get() = originalMenu.actor?.memberIn(context.guild)?.hasAuthority(AuthorityLevel.BOT_ADMIN) ?: false

            override fun reactEvent() {

                redirectTo(object : OptionMenu<String>(bot, context, listOf(context.guild.name)) {

                    init {
                        hook(object : SimpleTextHook(this, "Or, get name...") {

                            override fun hookEvent(hookContext: MessageContext) {
                                hookContext.gameNetwork.id = hookContext.message.contentStripped
                                close(false)
                            }

                            override fun isValid(hookContext: MessageContext): Boolean {
                                return super.isValid(hookContext) && bot.networkList.none { network -> network.id == hookContext.message.contentStripped }
                            }

                        })
                    }

                    override fun select(option: String) {
                        context.gameNetwork.id = option
                        close(false)
                    }

                }, true)

            }

        })

        menuResponses.add(object : MenuResponse("\uD83C\uDF10", "Set as main network") {
            override fun reactEvent() {

                context.gameNetwork.setAsMain()
                close(false)

            }

            override val isValid: Boolean
                get() = originalMenu.actor?.memberIn(context.guild)?.hasAuthority(AuthorityLevel.BOT_ADMIN) ?: false

        })

        menuResponses.add(object : MenuResponse("\uD83D\uDD17", "Merge this guild with other network") {

            override val isValid: Boolean
                get() = originalMenu.actor?.memberIn(context.guild)?.hasAuthority(AuthorityLevel.BOT_ADMIN) ?: false

            override fun reactEvent() {

                val networks: MutableList<GameNetwork> = bot.networkList.toMutableList()
                networks.remove(context.gameNetwork)

                redirectTo(object : OptionMenu<GameNetwork>(bot, context, networks) {

                    //TODO check if the new network doesn't have this nations' color taken yet
                    //TODO check if the new network has room for another guild (capacity is 8, one per color)
                    override fun select(selectedNetwork: GameNetwork) {
                        context.guild.moveToNetwork(selectedNetwork)
                        close(false)
                    }

                    override val embedBuilder: EmbedBuilder
                        get() = super.embedBuilder.setDescription(":warning: This guild will no longer be able to access this network's map. " +
                                "If you wish to keep this map and still merge, the other guild(s) must merge with this network instead.")

                }, true)

            }

        })
    }

    override val embedBuilder: EmbedBuilder
        get() = EmbedBuilder()
                .setTitle("Network Settings")
}