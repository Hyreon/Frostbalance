package botmanager.frostbalance.command;

import botmanager.frostbalance.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GuildMessageContext extends MessageContext {

    public GuildMessageContext(Frostbalance bot, PrivateMessageReceivedEvent privateEvent) {
        super(bot, privateEvent);
        if (!hasGuild()) throw new IllegalStateException("Tried to create a GuildCommandContext on a user without a default guild!");
    }

    public GuildMessageContext(Frostbalance bot, GuildMessageReceivedEvent publicEvent) {
        super(bot, publicEvent);
    }

    public GuildMessageContext(Frostbalance bot, Event genericEvent) {
        super(bot, genericEvent);
        if (!hasGuild()) throw new IllegalStateException("Tried to create a GuildCommandContext on a user without a default guild!");
    }

    public GuildMessageContext(MessageContext messageContext) {
        super(messageContext.bot, messageContext.getEvent());
        if (!hasGuild()) throw new IllegalStateException("Tried to create a GuildCommandContext on a user without a default guild!");
    }

    @NotNull @Deprecated
    public Guild getJDAGuild() {
        if (isPublic()) {
            return publicEvent.getGuild();
        } else return Objects.requireNonNull(getJDA().getGuildById(Objects.requireNonNull(getAuthor().getDefaultGuild()).getId()));
    }

    @NotNull
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
        return getJDAGuild().getMember(getJdaUser());
    }

    public MemberWrapper getMember() {
        return getAuthor().memberIn(getGuildId());
    }

    @Override
    public AuthorityLevel getAuthority() {
        return getMember().getAuthority();
    }

    public GameNetwork getGameNetwork() {
        return getGuild().getGameNetwork();
    }

    public Player getPlayer() {
        return getAuthor().playerIn(getGameNetwork());
    }
}
