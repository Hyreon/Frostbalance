package botmanager.frostbalance.command;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.GuildWrapper;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.menu.option.ListMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import java.util.List;
import java.util.Objects;

public class CommandContext {

    Frostbalance bot;

    PrivateMessageReceivedEvent privateEvent;
    GuildMessageReceivedEvent publicEvent;

    public CommandContext(Frostbalance bot, PrivateMessageReceivedEvent privateEvent) {
        this.bot = bot;
        this.privateEvent = privateEvent;
    }

    public CommandContext(Frostbalance bot, GuildMessageReceivedEvent publicEvent) {
        this.bot = bot;
        this.publicEvent = publicEvent;
    }

    public CommandContext(Frostbalance bot, Event genericEvent) {
        if (genericEvent instanceof PrivateMessageReceivedEvent) {
            this.bot = bot;
            this.privateEvent = (PrivateMessageReceivedEvent) genericEvent;
        } else if (genericEvent instanceof GuildMessageReceivedEvent) {
            this.bot = bot;
            this.publicEvent = (GuildMessageReceivedEvent) genericEvent;
        } else throw new IllegalStateException("CommandContext cannot be initialized with this sort of event!");
    }

    public boolean isPublic() {
        if (publicEvent != null) return true;
        if (privateEvent != null) return false;
        throw new IllegalStateException("CommandContext is neither public nor private, as no valid event was found!");
    }

    public JDA getJDA() {
        if (isPublic()) {
            return publicEvent.getJDA();
        }
        return privateEvent.getJDA();
    }

    @Deprecated
    public User getJDAUser() {
        if (isPublic()) {
            return publicEvent.getAuthor();
        }
        return privateEvent.getAuthor();
    }

    public UserWrapper getAuthor() {
        return bot.getUserWrapper(getJDAUser().getId());
    }

    public Message getMessage() {
        if (isPublic()) {
            return publicEvent.getMessage();
        }
        return privateEvent.getMessage();
    }

    public MessageChannel getChannel() {
        if (isPublic()) {
            return publicEvent.getChannel();
        }
        return privateEvent.getChannel();
    }

    public MessageChannel getPrivateChannel() {
        return Objects.requireNonNull(getAuthor().getJdaUser()).openPrivateChannel().complete();
    }

    public void sendResponse(String message) {
        MessageEmbed messageEmbed = buildEmbed(message);
        if (isPublic()) {
            Utilities.sendGuildMessage((TextChannel) getChannel(), messageEmbed);
        } else {
            Utilities.sendPrivateMessage(getJDAUser(), messageEmbed);
        }
    }

    public void sendEmbedResponse(List<String> resultLines) {
        if (resultLines.size() > 10) {
            new ListMenu<String>(bot, this, resultLines) {

            }.send(getChannel(), getAuthor());
        } else {
            String message = String.join("\n", resultLines);
            MessageEmbed messageEmbed = buildEmbed(message);
            if (isPublic()) {
                Utilities.sendGuildMessage((TextChannel) getChannel(), messageEmbed);
            } else {
                Utilities.sendPrivateMessage(getJDAUser(), messageEmbed);
            }
        }
    }

    public MessageEmbed buildEmbed(String message) {
        return new EmbedBuilder()
                .setDescription(message)
                .setColor(getGuild() != null ?
                        getGuild().getColor()
                        : null)
                .build();
    }

    public void sendPrivateResponse(String message) {
        MessageEmbed messageEmbed = buildEmbed(message);
        Utilities.sendPrivateMessage(getJDAUser(), messageEmbed);
    }


    public AuthorityLevel getAuthority() {
        if (hasGuild()) {
            return new GuildCommandContext(this).getAuthority();
        }
        return getAuthor().getAuthority();
    }

    public Event getEvent() {
        if (isPublic()) return publicEvent;
        else return privateEvent;
    }

    public boolean hasGuild() {
        if (isPublic()) {
            return true;
        } else {
            System.out.println(getAuthor().getDefaultGuildId());
            return getAuthor().getDefaultGuildId() != null;
        }
    }

    public GuildWrapper getGuild() {
        if (!hasGuild()) return null;
        else {
            return new GuildCommandContext(this).getGuild();
        }
    }
}
