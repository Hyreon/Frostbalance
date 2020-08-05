package botmanager.frostbalance.command;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public abstract class FrostbalanceSplitCommandBase extends FrostbalanceCommandBase {

    public FrostbalanceSplitCommandBase(Frostbalance bot, String[] keywords, AuthorityLevel authorityLevel, Condition... conditions) {
        super(bot, keywords, authorityLevel, conditions);
    }

    public FrostbalanceSplitCommandBase(Frostbalance bot, String[] keywords, AuthorityLevel authorityLevel) {
        super(bot, keywords, authorityLevel);
    }

    public FrostbalanceSplitCommandBase(Frostbalance bot, String[] keywords) {
        this(bot, keywords, AuthorityLevel.GENERIC);
    }

    public FrostbalanceSplitCommandBase(Frostbalance bot) {
        this(bot, null, AuthorityLevel.GENERIC);
    }

    @Override
    public void execute(GenericMessageReceivedEventWrapper eventWrapper, String[] params) {
        String message = String.join(" ", params);

        Event genericEvent = eventWrapper.getEvent();

        if (genericEvent instanceof GuildMessageReceivedEvent) {
            GuildMessageReceivedEvent event = (GuildMessageReceivedEvent) genericEvent;
            runPublic(event, message);
        } else if (genericEvent instanceof PrivateMessageReceivedEvent) {
            PrivateMessageReceivedEvent event = (PrivateMessageReceivedEvent) genericEvent;
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
