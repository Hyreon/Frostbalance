package botmanager.frostbalance.command;

import botmanager.frostbalance.Frostbalance;
import botmanager.generic.ICommand;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class FrostbalanceCommandBase implements ICommand {

    public static final boolean SPEEDTESTS = true;

    protected final String[] KEYWORDS;

    protected final List<Condition> CONDITIONS;

    protected final AuthorityLevel AUTHORITY_LEVEL;

    protected Frostbalance bot;

    public FrostbalanceCommandBase(Frostbalance bot, String[] keywords, AuthorityLevel authorityLevel, Condition... conditions) {
        this.bot = bot;
        KEYWORDS = keywords;
        CONDITIONS = new ArrayList(Arrays.asList(conditions));
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
        if (parameters.length == 1 && parameters[0].isEmpty()) {
            parameters = new String[] {};
        }

        if (!wouldAuthorize(eventWrapper.getAuthority())) {
            eventWrapper.sendResponse("You don't have sufficient privileges to do this.");
            return;
        }

        if (CONDITIONS.contains(Condition.PRIVATE) && eventWrapper.isPublic()) {
            eventWrapper.sendResponse("This command can only be run via DM.");
            return;
        }

        if (CONDITIONS.contains(Condition.PUBLIC) && !eventWrapper.isPublic()) {
            eventWrapper.sendResponse("This command can only be run publicly in servers.");
            return;
        }

        if (CONDITIONS.contains(Condition.GUILD_EXISTS) && !eventWrapper.getGuild().isPresent()) {
            eventWrapper.sendResponse("This command only works if you have a default guild set for DM. Set it with `.guild GUILD`.");
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
     * Does this user, with this guild, have the authority to run this command at its lowest authority level?
     * @return Whether the user could run this command
     */
    public boolean wouldAuthorize(AuthorityLevel authorityLevel) {
        return authorityLevel.hasAuthority(AUTHORITY_LEVEL);
    }

    /**
     * Gets the public info of a thing. This is predefined to actually use the internal info command
     * and wrap around it, not showing anything the user doesn't have authority to see.
     * @param context the context of the info gotten
     * @return
     */
    public Optional<String> getInfo(GenericMessageReceivedEventWrapper context) {
        if (context.getAuthority().hasAuthority(AUTHORITY_LEVEL) && (!CONDITIONS.contains(Condition.GUILD_EXISTS) || context.getGuildId().isPresent())) {
            return Optional.ofNullable(info(context.getAuthority(), context.isPublic()));
        } else return Optional.empty();
    }

    protected abstract String info(AuthorityLevel authorityLevel, boolean isPublic);

    public enum Condition {
        GUILD_EXISTS, PUBLIC, PRIVATE
    }
}
