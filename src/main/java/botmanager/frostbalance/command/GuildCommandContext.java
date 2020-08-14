package botmanager.frostbalance.command;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.GuildWrapper;
import botmanager.frostbalance.MemberWrapper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GuildCommandContext extends CommandContext {

    public GuildCommandContext(Frostbalance bot, PrivateMessageReceivedEvent privateEvent) {
        super(bot, privateEvent);
        if (!hasGuild()) throw new IllegalStateException("Tried to create a GuildCommandContext on a user without a default guild!");
    }

    public GuildCommandContext(Frostbalance bot, GuildMessageReceivedEvent publicEvent) {
        super(bot, publicEvent);
    }

    public GuildCommandContext(Frostbalance bot, Event genericEvent) {
        super(bot, genericEvent);
        if (!hasGuild()) throw new IllegalStateException("Tried to create a GuildCommandContext on a user without a default guild!");
    }

    public GuildCommandContext(CommandContext commandContext) {
        super(commandContext.bot, commandContext.getEvent());
        if (!hasGuild()) throw new IllegalStateException("Tried to create a GuildCommandContext on a user without a default guild!");
    }

    @NotNull @Deprecated
    public Guild getJDAGuild() {
        if (isPublic()) {
            return publicEvent.getGuild();
        } else return Objects.requireNonNull(getJDA().getGuildById(Objects.requireNonNull(getAuthor().getDefaultGuild()).getId()));
    }

    public GuildWrapper getGuild() {
        return bot.getGuildWrapper(getGuildId());
    }

    private String getGuildId() {
        if (isPublic()) {
            return publicEvent.getGuild().getId();
        }
        return getAuthor().getDefaultGuildId();
    }

    @Deprecated
    public Member getJDAMember() {
        if (isPublic()) {
            return publicEvent.getMember();
        }
        return getJDAGuild().getMember(getJDAUser());
    }

    public MemberWrapper getMember() {
        return bot.getMemberWrapper(getJDAUser().getId(), getJDAGuild().getId());
    }

    @Override
    public AuthorityLevel getAuthority() {
        return bot.getAuthority(getJDAGuild(), getJDAUser());
    }
}
