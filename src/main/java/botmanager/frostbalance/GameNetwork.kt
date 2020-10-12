package botmanager.frostbalance

import botmanager.frostbalance.flags.NetworkFlag
import botmanager.frostbalance.grid.Containable
import botmanager.frostbalance.grid.Container
import botmanager.frostbalance.grid.WorldMap
import java.util.*
import kotlin.collections.HashSet

class GameNetwork(@Transient var bot: Frostbalance, var id: String) : Containable<Frostbalance>, Container {

    private var turn: Int = 0
    var worldMap: WorldMap = WorldMap(this)
    var associatedGuilds: MutableSet<GuildWrapper> = HashSet()

    @Transient
    private var turnTimer = Timer()

    init {
        initializeTurnCycle()
    }

    fun initializeTurnCycle() {
        turnTimer = turnTimer ?: Timer() //impossible condition test
        turnTimer.schedule(object : TimerTask() {
            override fun run() {
                println("Doing turn $turn")
                worldMap.loadedTiles.forEach {
                    if (
                            it.objects.any { mob -> mob.doTurn(turn) }) { //something changed on the tile
                        //TODO update observers of this tile
                    }
                }
                turn++
            }
        }, 20000, 20000)
        println("Scheduled turn timer for $this!")
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