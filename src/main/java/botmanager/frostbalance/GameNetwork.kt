package botmanager.frostbalance

import botmanager.frostbalance.flags.NetworkFlag
import botmanager.frostbalance.grid.Containable
import botmanager.frostbalance.grid.Container
import botmanager.frostbalance.grid.WorldMap
import botmanager.frostbalance.grid.building.WorkManager
import java.util.*
import kotlin.collections.HashSet

class GameNetwork(@Transient var bot: Frostbalance, var id: String) : Containable<Frostbalance>, Container {

    private var turn: Int = 0
    var worldMap: WorldMap = WorldMap(this)
    var associatedGuilds: MutableSet<GuildWrapper> = HashSet()

    @Transient
    private val turnTimer = Timer()

    init {
        turnTimer.schedule(object : TimerTask() {
            override fun run() {
                worldMap.loadedTiles.forEach {
                    if (it.buildingData.buildings.any { building -> building.doTurn(turn) } ||
                            it.mobs.any { mob -> mob.doTurn(turn) }) { //something changed on the tile
                        //TODO update observers of this tile
                    }
                }
                WorkManager.singleton.validateWorkers()
            }
        }, 240000, 240000)
        turn++
    }

    val nations: Set<Nation>
        get() = associatedGuilds.map { guild -> guild.nation }.toHashSet()

    val players: List<Player>
        get() = bot.userWrappers.mapNotNull { user -> user.playerIfIn(this) }

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
        worldMap.setParent(this)
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
        return associatedGuilds.size > 1
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