package botmanager.frostbalance.command;

import botmanager.Utilities;
import botmanager.frostbalance.GuildWrapper;
import botmanager.frostbalance.MemberWrapper;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.Frostbalance;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import java.util.Optional;

public class GenericMessageReceivedEventWrapper {

    Frostbalance bot;

    PrivateMessageReceivedEvent privateEvent;
    GuildMessageReceivedEvent publicEvent;

    public GenericMessageReceivedEventWrapper(Frostbalance bot, PrivateMessageReceivedEvent privateEvent) {
        this.bot = bot;
        this.privateEvent = privateEvent;
    }

    public GenericMessageReceivedEventWrapper(Frostbalance bot, GuildMessageReceivedEvent publicEvent) {
        this.bot = bot;
        this.publicEvent = publicEvent;
    }

    public GenericMessageReceivedEventWrapper(Frostbalance bot, Event genericEvent) {
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

    public Optional<Guild> getGuild() {
        if (isPublic()) {
            return Optional.of(publicEvent.getGuild());
        }
        System.out.println("botUser " + getBotUser().getUserId());
        System.out.println("defaultBotGuild " + getBotUser().getDefaultBotGuild());
        return getBotUser().getDefaultBotGuild().map(guildWrapper -> getJDA().getGuildById(guildWrapper.getGuildId()));
    }

    public Optional<String> getGuildId() {
        if (isPublic()) {
            return Optional.of(publicEvent.getGuild().getId());
        }
        return getBotUser().getDefaultGuildId();
    }

    public Optional<GuildWrapper> getBotGuild() {
        return getGuildId().map(guildId -> bot.getGuild(guildId));
    }

    public Optional<Member> getMember() {
        if (isPublic()) {
            return Optional.ofNullable(publicEvent.getMember());
        }
        return getGuild().map(guild -> guild.getMember(getAuthor()));
    }

    public Optional<MemberWrapper> getBotMember() {
        return getGuild().map(guild -> bot.getMember(getAuthor().getId(), guild.getId()));
    }

    public User getAuthor() {
        if (isPublic()) {
            return publicEvent.getAuthor();
        }
        return privateEvent.getAuthor();
    }

    public UserWrapper getBotUser() {
        return bot.getUser(getAuthor().getId());
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
