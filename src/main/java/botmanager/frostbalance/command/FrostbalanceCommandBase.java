package botmanager.frostbalance.command;

import botmanager.frostbalance.Frostbalance;
import botmanager.generic.ICommand;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class FrostbalanceCommandBase implements ICommand {

    public static final boolean SPEED_TESTS = true;

    protected final String[] aliases;

    @Deprecated
    protected final List<Condition> conditions;

    protected final AuthorityLevel requiredAuthority;

    protected Frostbalance bot;

    public FrostbalanceCommandBase(Frostbalance bot, String[] aliases, AuthorityLevel authorityLevel, Condition... conditions) {
        this.bot = bot;
        this.aliases = aliases;
        this.conditions = new ArrayList(Arrays.asList(conditions));
        requiredAuthority = authorityLevel;
    }

    /**
     * Standard command strucuture. Execute can imply any number of things.
     * @param genericEvent
     */
    @Override
    public void run(Event genericEvent) {

        String[] parameters;

        CommandContext context;
        try {
            context = new CommandContext(bot, genericEvent);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }

        if (!hasAlias(genericEvent)) return;
        parameters = minifyMessage(context.getMessage().getContentRaw()).split(" ");
        if (parameters.length == 1 && parameters[0].isEmpty()) {
            parameters = new String[] {};
        }

        if (!wouldAuthorize(context.getAuthority())) {
            context.sendResponse("You don't have sufficient privileges to do this.");
            return;
        }

        if (conditions.contains(Condition.PRIVATE) && context.isPublic()) {
            context.sendResponse("This command can only be run via DM.");
            return;
        }

        if (conditions.contains(Condition.PUBLIC) && !context.isPublic()) {
            context.sendResponse("This command can only be run publicly in servers.");
            return;
        }

        if (SPEED_TESTS) {

            long startTime = System.nanoTime();

            execute(context, parameters);

            long stopTime = System.nanoTime();
            long elapsedTime = stopTime - startTime;
            System.out.println(elapsedTime + " nanoseconds to execute " + getClass().getCanonicalName());
        } else {

            execute(context, parameters);

        }
    }

    protected abstract void execute(CommandContext eventWrapper, String[] params);

    public boolean hasAlias(Event genericEvent) {
        String message;

        if (genericEvent instanceof GuildMessageReceivedEvent) {
            message = ((GuildMessageReceivedEvent) genericEvent).getMessage().getContentRaw();
        } else if (genericEvent instanceof PrivateMessageReceivedEvent) {
            message = ((PrivateMessageReceivedEvent) genericEvent).getMessage().getContentRaw();
        } else {
            return false;
        }

        for (String alias : aliases) {
            alias = bot.getPrefix() + alias;
            if (message.equalsIgnoreCase(alias)) {
                return true;
            } else if (message.startsWith(alias + " ")) {
                return true;
            }
        }

        return false;

    }

    public String minifyMessage(String message) {

        for (String keyword : aliases) {
            keyword = bot.getPrefix() + keyword;
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
        return authorityLevel.hasAuthority(requiredAuthority);
    }

    /**
     * Gets the public info of a thing. This is predefined to actually use the internal info command
     * and wrap around it, not showing anything the user doesn't have authority to see.
     * @param context the context of the info gotten
     * @return
     */
    public String getInfo(CommandContext context) {
        if (context.getAuthority().hasAuthority(requiredAuthority)) {
            return info(context.getAuthority(), context.isPublic());
        } else return null;
    }

    protected abstract String info(AuthorityLevel authorityLevel, boolean isPublic);

    public enum Condition {

        @Deprecated
        GUILD_EXISTS,

        @Deprecated
        PUBLIC,

        @Deprecated
        PRIVATE
    }
}
