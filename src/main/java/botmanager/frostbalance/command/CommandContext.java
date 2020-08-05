package botmanager.frostbalance.command;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.GuildWrapper;
import botmanager.frostbalance.MemberWrapper;
import botmanager.frostbalance.UserWrapper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        } else throw new IllegalStateException("GenericMessageReceivedEventWrapper cannot be initialized with this sort of event!");
    }

    public boolean isPublic() {
        if (publicEvent != null) return true;
        if (privateEvent != null) return false;
        throw new IllegalStateException("GenericMessageReceivedEventWrapper is neither public nor private, as no valid event was found!");
    }

    public JDA getJDA() {
        if (isPublic()) {
            return publicEvent.getJDA();
        }
        return privateEvent.getJDA();
    }

    public @Nullable Guild getGuild() {
        if (isPublic()) {
            return publicEvent.getGuild();
        }
        System.out.println("botUser " + getBotUser().getUserId());
        System.out.println("defaultBotGuild " + getBotUser().getDefaultBotGuild());
        return getJDA().getGuildById(getBotUser().getDefaultBotGuild().getGuildId());
    }

    public @Nullable String getGuildId() {
        if (isPublic()) {
            return publicEvent.getGuild().getId();
        }
        return getBotUser().getDefaultGuildId();
    }

    public @Nullable GuildWrapper getBotGuild() {
        return bot.getGuildWrapper(getGuildId());
    }

    public @Nullable Member getMember() {
        if (isPublic()) {
            return publicEvent.getMember();
        }
        return getGuild().getMember(getAuthor());
    }

    public @Nullable MemberWrapper getBotMember() {
        return bot.getMemberWrapper(getAuthor().getId(), getGuild().getId());
    }

    public User getAuthor() {
        if (isPublic()) {
            return publicEvent.getAuthor();
        }
        return privateEvent.getAuthor();
    }

    public UserWrapper getBotUser() {
        return bot.getUserWrapper(getAuthor().getId());
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

    public void sendResponse(String message) {
        if (isPublic()) {
            Utilities.sendGuildMessage((TextChannel) getChannel(), message);
        } else {
            Utilities.sendPrivateMessage(getAuthor(), message);
        }
    }

    public void sendResponse(List<String> resultLines) {
        sendResponse(String.join("\n", resultLines));
    }

    public void sendPrivateResponse(String message) {
        Utilities.sendPrivateMessage(getAuthor(), message);
    }


    public AuthorityLevel getAuthority() {
        return bot.getAuthority(getGuild(), getAuthor());
    }

    public Event getEvent() {
        if (isPublic()) return publicEvent;
        else return privateEvent;
    }
}
