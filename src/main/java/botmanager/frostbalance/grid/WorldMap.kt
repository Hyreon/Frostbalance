package botmanager.frostbalance.grid

import botmanager.IOUtils
import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.GameNetwork
import botmanager.frostbalance.Player
import botmanager.frostbalance.grid.coordinate.Hex
import botmanager.frostbalance.resource.ResourceDepositType
import com.google.gson.GsonBuilder
import net.dv8tion.jda.api.entities.Guild
import java.io.File
import java.util.*
import java.util.stream.Collectors

/**
 * A worldmap consists of a set of tiles and all data relevant to them.
 */
class WorldMap(@Transient var gameNetwork: GameNetwork) : Containable<GameNetwork>, Container {

    val resourceDepositTypes: List<ResourceDepositType>
        get() = gameNetwork.bot.globalResources()

    @Transient
    var highestLevelClaimData: ClaimData? = null
    var loadedTiles: MutableList<Tile> = ArrayList()

    var seed: Long = Random().nextLong()

    val players: List<PlayerCharacter>
    get() = {
        var list: MutableList<PlayerCharacter> = ArrayList()
        loadedTiles.forEach{ tile -> list.addAll(tile.objects.filterIsInstance(PlayerCharacter::class.java))}
        list
    }.invoke()

    fun getCharacter(player: Player): PlayerCharacter {
        return players.firstOrNull{ character -> character.userId == player.userWrapper.id} ?: let {
            return PlayerCharacter(player.userWrapper.id, it) //automatically added to players.
        }
    }

    /**
     * Gets an instance of the tile. This will *not*
     * create a new tile internally if the tile is empty,
     * so this method should not be used if you intend to
     * modify the tile with this method!
     * @param hex The location of the tile to render
     * @return
     */
    fun getRenderTile(hex: Hex): Tile {
        for (tile in loadedTiles) {
            if (tile.getLocation() == hex) {
                return tile
            }
        }
        return Tile(this, hex)
    }

    fun getTile(hex: Hex): Tile {
        for (tile in loadedTiles) {
            if (tile.getLocation() == hex) {
                return tile
            }
        }
        val newTile = Tile(this, hex)
        loadedTiles.add(newTile)
        return newTile
    }

    private fun clearEmptyTiles() {
        loadedTiles = loadedTiles.stream().filter { tile: Tile -> !tile.isEmpty }.collect(Collectors.toList())
    }

    val highestLevelClaim: ClaimData?
        get() {
            updateHighestLevelClaim()
            return highestLevelClaimData
        }

    fun updateHighestLevelClaim() {
        highestLevelClaimData = loadedTiles.maxByOrNull { tile -> tile.claimData.claimLevel }
                ?.claimData
    }

    override fun adopt() {
        for (tile in loadedTiles) {
            tile.setParent(this)
            tile.adopt()
        }
    }

    fun isEmpty(): Boolean {
        clearEmptyTiles()
        return loadedTiles.isEmpty()
    }

    companion object {
        @Deprecated("")
        var worldMaps: MutableList<WorldMap> = ArrayList()

        @JvmStatic
        @Deprecated("")
        operator fun get(guild: Guild): WorldMap {
            return Frostbalance.bot.getGuildWrapper(guild.id).gameNetwork.worldMap
        }

        @Deprecated("")
        fun readWorldLegacy(originalGuildId: String?) {
            var effectiveGuildId: String = originalGuildId ?: "global"
            var networkName: String = originalGuildId ?: "main"
            println("New guild name: $effectiveGuildId")
            val file = File("data/" + Frostbalance.bot.name + "/$effectiveGuildId/map.json")
            if (file.exists()) {
                val gsonBuilder = GsonBuilder()
                gsonBuilder.registerTypeAdapter(TileObject::class.java, TileObjectAdapter())
                val gson = gsonBuilder.create()
                val worldMap = gson.fromJson(IOUtils.read(file), WorldMap::class.java)
                Frostbalance.bot.getGameNetwork(networkName).worldMap = worldMap
                Frostbalance.bot.getGameNetwork(networkName).adopt()
                for (tile in worldMap.loadedTiles) {
                    tile.map = worldMap
                    for (tileData in tile.getObjects()) {
                        tileData.tile = tile
                    }
                    tile.getClaimData().tile = tile
                    for (claim in tile.claimData.claims) {
                        claim.claimData = tile.claimData
                    }
                    println("Loaded data for tile at " + tile.getLocation() + " (" + effectiveGuildId + ")")
                }
                println("Added $networkName world map to worldMaps list.")
                worldMaps.add(worldMap)
            } else {
                throw IllegalStateException("Specified file does not exist")
            }
        }

        @Deprecated("")
        fun writeWorld(guild: Guild?, map: WorldMap) {
            val guildId: String
            guildId = guild?.id ?: "global"
            map.clearEmptyTiles()
            val file = File("data/" + Frostbalance.bot.name + "/" + guildId + "/map.json")
            val gsonBuilder = GsonBuilder()
            gsonBuilder.registerTypeAdapter(TileObject::class.java, TileObjectAdapter())
            val gson = gsonBuilder.create()
            IOUtils.write(file, gson.toJson(map))
        }
    }

    override fun setParent(parent: GameNetwork) {
        while (seed == 0L) {
            seed = Random().nextLong()
        }
        this.gameNetwork = parent
    }
}