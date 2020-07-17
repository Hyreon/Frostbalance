package botmanager.frostbalance.generic;

import botmanager.Utilities;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public abstract class FrostbalanceHybridCommandBase extends FrostbalanceCommandBase {

    public FrostbalanceHybridCommandBase(BotBase bot, String[] keywords, AuthorityLevel authorityLevel) {
        super(bot, keywords, authorityLevel);
    }

    @Override
    public void run(Event genericEvent) {
        String message;
        GenericMessageReceivedEventWrapper eventWrapper;

        if (genericEvent instanceof GuildMessageReceivedEvent) {
            message = ((GuildMessageReceivedEvent)genericEvent).getMessage().getContentRaw();
        } else if (genericEvent instanceof PrivateMessageReceivedEvent) {
            message = ((PrivateMessageReceivedEvent)genericEvent).getMessage().getContentRaw();
        } else {
            return;
        }

        if (!hasKeywords(genericEvent)) return;
        message = minifyMessage(message);

        eventWrapper = new GenericMessageReceivedEventWrapper(bot, genericEvent);

        if (genericEvent instanceof GuildMessageReceivedEvent) {
            GuildMessageReceivedEvent event = (GuildMessageReceivedEvent) genericEvent;
            if (!wouldAuthorize(event.getGuild(), event.getAuthor())) {
                Utilities.sendGuildMessage(event.getChannel(), "You don't have sufficient privileges to do this.");
                return;
            }
            runHybrid(new GenericMessageReceivedEventWrapper(bot, event), message);
        } else if (genericEvent instanceof PrivateMessageReceivedEvent) {
            PrivateMessageReceivedEvent event = (PrivateMessageReceivedEvent) genericEvent;
            if (!wouldAuthorize(eventWrapper.getGuild(), event.getAuthor())) {
                Utilities.sendPrivateMessage(event.getAuthor(), "You don't have sufficient privileges to do this.");
                return;
            }
            runHybrid(eventWrapper, message);
        }
    }

    protected abstract void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String message);

}
