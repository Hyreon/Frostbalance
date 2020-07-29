package botmanager.frostbalance.grid;

import net.dv8tion.jda.api.entities.Guild;

public class GuildWorldMap extends WorldMap {

    /**
     * The guild this map is tied to.
     */
    String guildId;

    public GuildWorldMap(Guild guild) {
        super(guild);
    }

}
