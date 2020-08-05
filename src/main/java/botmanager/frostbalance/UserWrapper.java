package botmanager.frostbalance;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A special user class that contains data about a player that is unique to Frostbalance.
 * Extension is not an option, as User is actually an interface. Additionally, BotUsers are not
 * guaranteed to share any server with the main bot; this is intentional, so that players who
 * cease to play (or are banned) do not cease the game's functionality.
 */
public class UserWrapper {

    transient Frostbalance bot;

    @Getter
    String userId;

    /**
     * A list of a member instances of this player.
     */
    List<MemberWrapper> memberReference = new ArrayList<>();

    transient BufferedImage userIcon;

    @Getter
    Optional<String> lastKnownName;

    @Getter @Setter
    Nation allegiance = Nation.NONE;
    @Getter
    Optional<String> defaultGuildId = Optional.empty();
    boolean globallyBanned = false;

    public UserWrapper(Frostbalance bot, User user) {
        Objects.requireNonNull(bot);
        Objects.requireNonNull(user);
        this.bot = bot;
        this.userId = user.getId();

        lastKnownName = Optional.of(user.getName());

    }

    /**
     * Gets this user as a member of the guild with the specified id.
     * @param id The id of the guild this user is supposedly a member of.
     * @return The MemberWrapper referencing this UserWrapper and a GuildId
     */
    public MemberWrapper getMember(String id) {
        Optional<MemberWrapper> botMember = memberReference.stream().filter(member -> member.getGuildId().equals(id)).findFirst();
        if (!botMember.isPresent()) {
            Objects.requireNonNull(getJDA().getGuildById(id));
            botMember = Optional.of(new MemberWrapper(bot, this, id));
            memberReference.add(botMember.get());
        }
        return botMember.get();
    }

    public Optional<User> getUser() {
        return Optional.ofNullable(bot.getJDA().getUserById(userId));
    }

    public Optional<GuildWrapper> getDefaultBotGuild() {
        return defaultGuildId.map(
                id -> bot.getGuild(id));
    }

    public JDA getJDA() {
        return bot.getJDA();
    }

    public String getName() {
        return getUser().map(user -> user.getName()).orElse("Deleted User");
    }

    public void resetDefaultGuild() {
        defaultGuildId = Optional.empty();
    }

    public void setDefaultGuildId(String id) {
        defaultGuildId = Optional.of(id);
    }
}
