package botmanager.frostbalance;

import botmanager.frostbalance.grid.Containable;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;

import java.util.Objects;
import java.util.Optional;

public class MemberWrapper implements Containable<UserWrapper> {

    @Getter
    transient UserWrapper userWrapper;

    @Getter
    String guildId;

    @Getter
    Optional<String> lastKnownNickname;

    @Getter
    Influence influence = new Influence(0);

    DailyInfluenceSource dailyInfluence = new DailyInfluenceSource();

    boolean banned = false;

    public MemberWrapper(Frostbalance bot, UserWrapper userWrapper, String guildId) {
        Objects.requireNonNull(bot);
        this.userWrapper = userWrapper;
        this.guildId = guildId;
        lastKnownNickname = Optional.empty();
    }

    public String getUserId() {
        return getUserWrapper().getUserId();
    }

    public void adjustInfluence(Influence amount) {
        influence = influence.add(amount);
        if (influence.isNegative()) {
            influence = Influence.none();
        }
    }

    public String getEffectiveName() {
        Optional<String> nickname = getMember().map(Member::getNickname);
        if (nickname.isPresent()) {
            lastKnownNickname = nickname;
        }
        return lastKnownNickname.orElse(userWrapper.getName());
    }

    /**
     *
     * @return The member if extant, or an empty optional if the bot has been removed from the relevant guild,
     * or the relevant player has left from it.
     */
    public Optional<Member> getMember() {
        return Optional.ofNullable(getJDA().getGuildById(getGuildId())).map(guild -> guild.getMemberById(getUserId()));
    }

    public GuildWrapper getGuildWrapper() {
        return userWrapper.bot.getGuild(guildId);
    }

    private JDA getJDA() {
        return getUserWrapper().getJDA();
    }

    public DailyInfluenceSource getInfluenceSource() {
        if (!dailyInfluence.isActive()) {
            dailyInfluence = new DailyInfluenceSource();
        }
        return dailyInfluence;
    }

    public boolean hasBeenForciblyRemoved() {
        return getGuildWrapper().hasBeenForciblyRemoved(getUserId());
    }
}
