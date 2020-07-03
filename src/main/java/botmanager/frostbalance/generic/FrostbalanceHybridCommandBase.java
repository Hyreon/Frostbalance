package botmanager.frostbalance.generic;

import botmanager.generic.BotBase;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public abstract class FrostbalanceHybridCommandBase extends FrostbalanceCommandBase {

    protected final String[] KEYWORDS;

    public FrostbalanceHybridCommandBase(BotBase bot, String[] keywords) {
        super(bot);
        KEYWORDS = keywords;
    }

    @Override
    public void run(Event genericEvent) {
        String message;
        boolean found = false;

        if (genericEvent instanceof GuildMessageReceivedEvent) {
            message = ((GuildMessageReceivedEvent)genericEvent).getMessage().getContentRaw();
        } else if (genericEvent instanceof PrivateMessageReceivedEvent) {
            message = ((PrivateMessageReceivedEvent)genericEvent).getMessage().getContentRaw();
        } else {
            return;
        }

        for (String keyword : KEYWORDS) {
            if (message.equalsIgnoreCase(keyword)) {
                message = message.replace(keyword, "");
                found = true;
                break;
            } else if (message.startsWith(keyword + " ")) {
                message = message.replace(keyword + " ", "");
                found = true;
                break;
            }
        }

        if (!found) {
            return;
        }

        if (genericEvent instanceof GuildMessageReceivedEvent) {
            runPublic((GuildMessageReceivedEvent) genericEvent, message);
        } else if (genericEvent instanceof PrivateMessageReceivedEvent) {
            runPrivate((PrivateMessageReceivedEvent) genericEvent, message);
        }
    }

    public abstract void runPublic(GuildMessageReceivedEvent event, String message);
    public abstract void runPrivate(PrivateMessageReceivedEvent event, String message);

}
