package botmanager.frostbalance.generic;

import botmanager.Utilities;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public abstract class FrostbalanceSplitCommandBase extends FrostbalanceCommandBase {

    public FrostbalanceSplitCommandBase(BotBase bot, String[] keywords, AuthorityLevel authorityLevel) {
        super(bot, keywords, authorityLevel);
    }

    public FrostbalanceSplitCommandBase(BotBase bot, String[] keywords) {
        this(bot, keywords, AuthorityLevel.GENERIC);
    }

    public FrostbalanceSplitCommandBase(BotBase bot) {
        this(bot, null, AuthorityLevel.GENERIC);
    }

    @Override
    public void run(Event genericEvent) {
        String message;

        if (genericEvent instanceof GuildMessageReceivedEvent) {
            message = ((GuildMessageReceivedEvent)genericEvent).getMessage().getContentRaw();
        } else if (genericEvent instanceof PrivateMessageReceivedEvent) {
            message = ((PrivateMessageReceivedEvent)genericEvent).getMessage().getContentRaw();
        } else {
            return;
        }

        if (!hasKeywords(genericEvent)) return;
        message = minifyMessage(message);

        if (genericEvent instanceof GuildMessageReceivedEvent) {
            GuildMessageReceivedEvent event = (GuildMessageReceivedEvent) genericEvent;

            if (!wouldAuthorize(event.getGuild(), event.getAuthor())) {
                Utilities.sendGuildMessage(event.getChannel(), "You need to have authority level " + AUTHORITY_LEVEL.name() + " to do this.");
                return;
            }

            runPublic(event, message);
        } else if (genericEvent instanceof PrivateMessageReceivedEvent) {
            PrivateMessageReceivedEvent event = (PrivateMessageReceivedEvent) genericEvent;

            if (!wouldAuthorize(new GenericMessageReceivedEventWrapper(bot, event).getGuild(), event.getAuthor())) {
                Utilities.sendPrivateMessage(event.getAuthor(), "You need to have authority level " + AUTHORITY_LEVEL.name() + " to do this.");
                return;
            }

            runPrivate(event, message);
        }
    }

    public void runPublic(GuildMessageReceivedEvent event, String message) {
        Utilities.sendPrivateMessage(event.getAuthor(), "This command doesn't work in public guilds.");
    }

    public void runPrivate(PrivateMessageReceivedEvent event, String message) {
        Utilities.sendPrivateMessage(event.getAuthor(), "This command doesn't work in private chat.");
    }

    public abstract String publicInfo(AuthorityLevel authorityLevel);
    public abstract String privateInfo(AuthorityLevel authorityLevel);

    @Override
    public String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (isPublic) {
            return publicInfo(authorityLevel);
        } else {
            return privateInfo(authorityLevel);
        }
    }
}
