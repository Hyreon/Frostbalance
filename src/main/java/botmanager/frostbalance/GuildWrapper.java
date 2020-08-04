package botmanager.frostbalance;

import botmanager.frostbalance.data.RegimeData;
import botmanager.frostbalance.grid.WorldMap;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuildWrapper {

    transient Frostbalance bot;

    @Getter
    String guildId;

    transient BufferedImage guildIcon;
    @Getter
    String lastKnownName;

    Optional<String> ownerId = Optional.empty();
    Optional<WorldMap> map = Optional.empty();
    List<OptionFlag> optionFlags = new ArrayList<>();
    List<RegimeData> regimes = new ArrayList<>();

    public GuildWrapper(Frostbalance bot, Guild guild) {
        this.bot = bot;
        this.guildId = guild.getId();
    }

    public String getName() {
        if (getJDA().getGuildById(guildId) != null) {
            lastKnownName = getJDA().getGuildById(guildId).getName();
        }
        return lastKnownName;
    }

    public Optional<Guild> getGuild() {
        return Optional.ofNullable(getJDA().getGuildById(guildId));
    }

    public JDA getJDA() {
        return bot.getJDA();
    }
}
