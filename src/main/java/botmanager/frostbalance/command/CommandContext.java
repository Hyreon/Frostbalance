package botmanager.frostbalance.command;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.UserWrapper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

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

    public void sendResponse(String message) {
        if (isPublic()) {
            Utilities.sendGuildMessage((TextChannel) getChannel(), message);
        } else {
            Utilities.sendPrivateMessage(getJDAUser(), message);
        }
    }

    public void sendResponse(List<String> resultLines) {
        sendResponse(String.join("\n", resultLines));
    }

    public void sendPrivateResponse(String message) {
        Utilities.sendPrivateMessage(getJDAUser(), message);
    }


    public AuthorityLevel getAuthority() {
        if (hasGuild()) {
            return bot.getAuthority(new GuildCommandContext(this).getJDAGuild(), getJDAUser());
        }
        return bot.getAuthority(getJDAUser());
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
}
