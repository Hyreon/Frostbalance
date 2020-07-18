package botmanager.frostbalance.generic;

import botmanager.frostbalance.Frostbalance;
import botmanager.generic.BotBase;
import botmanager.generic.ICommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public abstract class FrostbalanceCommandBase implements ICommand {

    public static final boolean SPEEDTESTS = true;

    protected final String[] KEYWORDS;

    protected final AuthorityLevel AUTHORITY_LEVEL;

    protected Frostbalance bot;

    public FrostbalanceCommandBase(BotBase bot, String[] keywords, AuthorityLevel authorityLevel) {
        this.bot = (Frostbalance) bot;
        KEYWORDS = keywords;
        AUTHORITY_LEVEL = authorityLevel;
    }

    /**
     * Standard command strucuture. Execute can imply any number of things.
     * @param genericEvent
     */
    @Override
    public void run(Event genericEvent) {

        String[] parameters;

        GenericMessageReceivedEventWrapper eventWrapper;
        try {
            eventWrapper = new GenericMessageReceivedEventWrapper(bot, genericEvent);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getStackTrace());
            return;
        }

        if (!hasKeywords(genericEvent)) return;
        parameters = minifyMessage(eventWrapper.getMessage().getContentRaw()).split(" ");

        if (!wouldAuthorize(eventWrapper.getGuild(), eventWrapper.getAuthor())) {
            eventWrapper.sendResponse("You don't have sufficient privileges to do this.");
            return;
        }

        if (SPEEDTESTS) {

            long startTime = System.nanoTime();

            execute(eventWrapper, parameters);

            long stopTime = System.nanoTime();
            long elapsedTime = stopTime - startTime;
            System.out.println(elapsedTime + " nanoseconds to execute " + getClass().getCanonicalName());
        } else {

            execute(eventWrapper, parameters);

        }
    }

    public abstract void execute(GenericMessageReceivedEventWrapper eventWrapper, String[] params);

    public boolean hasKeywords(Event genericEvent) {
        String message;

        if (genericEvent instanceof GuildMessageReceivedEvent) {
            message = ((GuildMessageReceivedEvent) genericEvent).getMessage().getContentRaw();
        } else if (genericEvent instanceof PrivateMessageReceivedEvent) {
            message = ((PrivateMessageReceivedEvent) genericEvent).getMessage().getContentRaw();
        } else {
            return false;
        }

        for (String keyword : KEYWORDS) {
            if (message.equalsIgnoreCase(keyword)) {
                return true;
            } else if (message.startsWith(keyword + " ")) {
                return true;
            }
        }

        return false;

    }

    public String minifyMessage(String message) {

        for (String keyword : KEYWORDS) {
            if (message.equalsIgnoreCase(keyword)) {
                return message.replace(keyword, "");
            } else if (message.startsWith(keyword + " ")) {
                return message.replace(keyword + " ", "");
            }
        }

        return null;

    }

    /**
     * Does this user, with this guild, have the authority to run this command as system?
     * @param guild The guild where the command would be run at
     * @param user The user running this command
     * @return Whether the user could run this command as system
     */
    public boolean wouldAuthorize(Guild guild, User user) {
        return bot.getAuthority(guild, user).hasAuthority(AUTHORITY_LEVEL);
    }

    public String getInfo(AuthorityLevel authorityLevel, boolean isPublic) {
        if (authorityLevel.hasAuthority(AUTHORITY_LEVEL)) {
            return info(authorityLevel, isPublic);
        } else return null;
    }

    public abstract String info(AuthorityLevel authorityLevel, boolean isPublic);

    public boolean isAdminOnly() {
        return AUTHORITY_LEVEL.hasAuthority(AuthorityLevel.BOT_ADMIN);
    }

}
