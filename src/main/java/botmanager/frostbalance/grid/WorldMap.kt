package botmanager.frostbalance.grid

import botmanager.IOUtils
import botmanager.frostbalance.Player
import botmanager.frostbalance.*
import com.google.gson.GsonBuilder
import net.dv8tion.jda.api.entities.Guild
import java.io.File
import java.util.*
import java.util.stream.Collectors

//TODO change worldMap to split into different types based on whether it's a main map, tutorial map, or private guild map.
/**
 * A worldmap consists of a set of tiles and all data relevant to them.
 */
class WorldMap(@Transient var gameNetwork: GameNetwork, var id: String) : Container {

    @Transient
    var strongestNationalClaim = Influence(0)
    var loadedTiles: MutableList<Tile> = ArrayList()

    val players: List<PlayerCharacter>
    get() = {
        var list: MutableList<PlayerCharacter> = ArrayList()
        loadedTiles.forEach{ tile -> list.addAll(tile.objects.filterIsInstance(PlayerCharacter::class.java))}
        list
    }.invoke()

    fun getCharacter(player: Player): PlayerCharacter {
        return players.firstOrNull{ character -> character.userId == player.userId} ?: let {
            return PlayerCharacter(player.userId, it)
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

    val strongestClaim: Influence
        get() {
            updateStrongestClaim()
            return strongestNationalClaim
        }

    fun updateStrongestClaim() {
        strongestNationalClaim = Influence(0)
        for (tile in loadedTiles) {
            val nationalStrength = tile.getClaimData().nationalStrength
            if (nationalStrength.compareTo(strongestNationalClaim) > 0) {
                strongestNationalClaim = nationalStrength
            }
        }
    }

    /**
     * Tests whether this map is part of the main map, shared between
     * the three main servers.
     * @return Whether this is the main map
     */
    @Deprecated("", ReplaceWith("gameNetwork.isMain()"))
    val isMainMap: Boolean
        get() = gameNetwork.isMain()
    @Deprecated("", ReplaceWith("gameNetwork.isTutorial()"))
    val isTutorialMap: Boolean
        get() = gameNetwork.isTutorial()

    override fun adopt() {
        for (tile in loadedTiles) {
            tile.setParent(this)
        }
    }

    companion object {
        @Deprecated("")
        var worldMaps: MutableList<WorldMap> = ArrayList()

        @JvmStatic
        @Deprecated("")
        operator fun get(guild: Guild?): WorldMap {
            return Frostbalance.bot.getGuildWrapper(guild!!.id).gameNetwork.worldMap
        }

        @get:Deprecated("")
        val maps: Collection<WorldMap>
            get() = worldMaps

        @Deprecated("")
        fun readWorld(guildId: String?) {
            var guildId = guildId
            if (guildId == null) {
                guildId = "global"
            }
            val file = File("data/" + Frostbalance.bot.name + "/" + guildId + "/map.json")
            if (file.exists()) {
                val gsonBuilder = GsonBuilder()
                gsonBuilder.registerTypeAdapter(TileObject::class.java, TileObjectAdapter())
                val gson = gsonBuilder.create()
                val worldMap = gson.fromJson(IOUtils.read(file), WorldMap::class.java)
                worldMap.id = guildId
                Frostbalance.bot.getGameNetwork(guildId).worldMap = worldMap
                Frostbalance.bot.getGameNetwork(guildId).adopt()
                for (tile in worldMap.loadedTiles) {
                    tile.map = worldMap
                    for (tileData in tile.getObjects()) {
                        tileData.tile = tile
                    }
                    tile.getClaimData().tile = tile
                    for (claim in tile.claimData.claims) {
                        claim.claimData = tile.claimData
                    }
                    println("Loaded data for tile at " + tile.getLocation() + " (" + guildId + ")")
                }
                println("Added " + worldMap.id + " world map to worldMaps list.")
                worldMaps.add(worldMap)
            }
            throw IllegalStateException("Specified file does not exist")
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
}