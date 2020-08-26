package botmanager.frostbalance

import botmanager.frostbalance.flags.NetworkFlag
import botmanager.frostbalance.grid.Containable
import botmanager.frostbalance.grid.Container
import botmanager.frostbalance.grid.WorldMap

class GameNetwork(@Transient var bot: Frostbalance, var id: String) : Containable<Frostbalance>, Container {

    var worldMap: WorldMap = WorldMap(this)
    var associatedGuilds: MutableSet<GuildWrapper> = HashSet()

    val nations: Set<Nation>
        get() = associatedGuilds.map { guild -> guild.nation }.toHashSet()

    private var flags: MutableSet<NetworkFlag> = HashSet()

    fun addGuild(guild: GuildWrapper) {
        associatedGuilds.add(guild)
        guild.gameNetwork = this
    }

    override fun setParent(parent: Frostbalance) {
        bot = parent
    }

    fun isMain(): Boolean {
        return bot.mainNetwork == this
    }

    fun isTutorial(): Boolean {
        return hasNetworkFlag(NetworkFlag.TUTORIAL)
    }

    private fun hasNetworkFlag(flag: NetworkFlag): Boolean {
        return flags.contains(flag)
    }

    fun addNetworkFlag(flag: NetworkFlag): Boolean {
        return flags.add(flag)
    }

    fun setAsMain() {
        bot.mainNetwork = this
    }

    override fun adopt() {
        worldMap.gameNetwork = this
        worldMap.adopt()
        associatedGuilds.forEach { guild ->
            guild.gameNetwork = this
            guild.adopt()
        }
    }

    fun guildWithAllegiance(allegiance: Nation?): GuildWrapper? {
        return associatedGuilds.firstOrNull { guild -> guild.nation == allegiance}
    }

    fun hasMultipleNations(): Boolean {
        return associatedGuilds.size > 1 || flags.contains(NetworkFlag.TUTORIAL)
    }

    /**
     * This function should only be called by a GuildWrapper.
     */
    fun moveGuild(guild: GuildWrapper, selectedNetwork: GameNetwork) {
        associatedGuilds.remove(guild)
        selectedNetwork.addGuild(guild)
    }

    override fun toString(): String {
        return id
    }

    fun isEmpty(): Boolean {
        return worldMap.isEmpty() && associatedGuilds.isEmpty()
    }

}