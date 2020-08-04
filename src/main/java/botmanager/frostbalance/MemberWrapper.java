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

    Influence influence = new Influence(0);

    DailyInfluenceSource dailyInfluence = new DailyInfluenceSource();

    boolean banned = false;

    public MemberWrapper(Frostbalance bot, Member member) {
        Objects.requireNonNull(bot);
        Objects.requireNonNull(member);
        this.userWrapper = bot.getUser(member.getId());
        this.guildId = member.getGuild().getId();
        lastKnownNickname = Optional.ofNullable(member.getNickname());
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
        return lastKnownNickname.orElse(userWrapper.getLastKnownName());
    }

    /**
     *
     * @return The member if extant, or an empty optional of the bot has been removed from the relevant guild,
     * or the relevant player has left from it.
     */
    public Optional<Member> getMember() {
        return Optional.ofNullable(getJDA().getGuildById(getGuildId())).map(guild -> guild.getMemberById(getUserId()));
    }

    private JDA getJDA() {
        return getUserWrapper().getJDA();
    }
}
