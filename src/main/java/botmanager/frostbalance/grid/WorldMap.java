package botmanager.frostbalance.grid;

import botmanager.IOUtils;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.OptionFlag;
import com.google.gson.*;
import net.dv8tion.jda.api.entities.Guild;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//TODO change worldMap to split into different types based on whether it's a main map, tutorial map, or private guild map.
/**
 * A worldmap consists of a set of tiles and all data relevant to them.
 */
public class WorldMap implements Container<Tile> {

    static List<WorldMap> worldMaps = new ArrayList<>();

    public static WorldMap get(Guild guild) {
        if (Frostbalance.bot.getSettings(guild).contains(OptionFlag.MAIN)) {
            guild = null;
        }
        for (WorldMap map : worldMaps) {
            if (map.getGuild() == null) {
                if (guild == null) return map;
            } else if (map.getGuild().equals(guild)) {
                return map;
            }
        }
        WorldMap newMap = new WorldMap(guild);
        worldMaps.add(newMap);
        return newMap;
    }

    transient Double strongestNationalClaim = Double.MIN_NORMAL;

    List<Tile> loadedTiles = new ArrayList<>();

    /**
     * The guild this map is tied to.
     * If null, that means this is the main/global map.
     */
    transient Guild guild;

    public WorldMap(Guild guild) {
        this.guild = guild;
    }

    public static Collection<WorldMap> getMaps() {
        return worldMaps;
    }


    public static void readWorld(Guild guild) {

        String guildId;
        if (guild == null) {
            guildId = "global";
        } else {
            guildId = guild.getId();
        }

        File file = new File("data/" + Frostbalance.bot.getName() + "/" + guildId + "/map.json");
        if (file.exists()) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(TileObject.class, new TileObjectAdapter());
            Gson gson = gsonBuilder.create();
            WorldMap worldMap = gson.fromJson(IOUtils.read(file), WorldMap.class);
            worldMap.guild = guild;
            for (Tile tile : worldMap.loadedTiles) {
                tile.map = worldMap;
                for (TileData tileData : tile.getObjects()) {
                    tileData.tile = tile;
                }
                tile.getClaimData().tile = tile;
                for (Claim claim : tile.claimData.claims) {
                    claim.claimData = tile.claimData;
                }
                System.out.println("Loaded data for tile at " + tile.getLocation() + " (" + guildId + ")");
            }
            System.out.println("Added " + worldMap.guild + " world map to worldMaps list.");
            worldMaps.add(worldMap);
        }

    }

    public static void writeWorld(Guild guild, WorldMap map) {
        String guildId;
        if (guild == null) {
            guildId = "global";
        } else {
            guildId = guild.getId();
        }

        File file = new File("data/" + Frostbalance.bot.getName() + "/" + guildId + "/map.json");
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(TileObject.class, new TileObjectAdapter());
        Gson gson = gsonBuilder.create();
        IOUtils.write(file, gson.toJson(map));
    }

    /**
     * The same as get tile, but the default tile it returns will
     * *not* be cached in the grid. Do NOT use this for functions that
     * change the tile!
     * @param hex
     * @return
     */
    @Deprecated
    public Tile getTileLazy(Hex hex) {
        for (Tile tile : loadedTiles) {
            if (tile.getLocation().equals(hex)) {
                return tile;
            }
        }
        return new Tile(this, hex);
    }

    public Tile getTile(Hex hex) {
        for (Tile tile : loadedTiles) {
            if (tile.getLocation().equals(hex)) {
                return tile;
            }
        }
        Tile newTile = new Tile(this, hex);
        loadedTiles.add(newTile);
        return newTile;
    }

    public double getStrongestClaim() {
        updateStrongestClaim();
        return strongestNationalClaim;
    }

    protected void updateStrongestClaim() {
        strongestNationalClaim = Double.MIN_NORMAL;
        for (Tile tile : loadedTiles) {
            Double nationalStrength = tile.getClaimData().getNationalStrength();
            if (nationalStrength > strongestNationalClaim) {
                strongestNationalClaim = nationalStrength;
            }
        }
    }

    /**
     * Tests whether this map is part of the main map, shared between
     * the three main servers.
     * @return Whether this is the main map
     */
    public boolean isMainMap() {
        if (guild == null) {
            return true;
        } else return false;
    }

    public boolean isTutorialMap() {
        if (!isMainMap() && Frostbalance.bot.getSettings(guild).contains(OptionFlag.TUTORIAL)) {
            return true;
        } else return false;
    }

    public Guild getGuild() {
        return guild;
    }

    @Override
    public Tile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Tile tile = context.deserialize(json, Tile.class);
        tile.map = this;
        return tile;
    }
}
