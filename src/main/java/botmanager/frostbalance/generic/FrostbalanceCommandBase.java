package botmanager.frostbalance.generic;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.generic.BotBase;
import botmanager.generic.ICommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public abstract class FrostbalanceCommandBase implements ICommand {

    protected final String[] KEYWORDS;

    protected final boolean ADMIN_ONLY;

    protected Frostbalance bot;

    public FrostbalanceCommandBase(BotBase bot, String[] keywords, boolean adminOnly) {
        this.bot = (Frostbalance) bot;
        KEYWORDS = keywords;
        ADMIN_ONLY = adminOnly;
    }

    public FrostbalanceCommandBase(BotBase bot, String[] keywords) {
        this(bot, keywords, false);
    }

    public FrostbalanceCommandBase(BotBase bot) {
        this(bot, null, false);
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
            GuildMessageReceivedEvent event = (GuildMessageReceivedEvent) genericEvent;
            if (ADMIN_ONLY && !event.getMember().getRoles().contains(bot.getSystemRole(event.getGuild()))) {
                Utilities.sendGuildMessage(event.getChannel(), "You must be a system administrator here to do this.");
                return;
            }
            runPublic(event, message);
        } else if (genericEvent instanceof PrivateMessageReceivedEvent) {
            PrivateMessageReceivedEvent event = (PrivateMessageReceivedEvent) genericEvent;
            if (ADMIN_ONLY && bot.hasSystemRoleEverywhere(event.getAuthor())) {
                Utilities.sendPrivateMessage(event.getAuthor(), "You must be a system administrator in all servers to do this.");
                return;
            }
            runPrivate(event, message);
        }
    }

    public void runPublic(GuildMessageReceivedEvent event, String message) {
        Utilities.sendPrivateMessage(event.getAuthor(), "This command doesn't work in public guilds.");
    };

    public void runPrivate(PrivateMessageReceivedEvent event, String message) {
        Utilities.sendPrivateMessage(event.getAuthor(), "This command doesn't work in private chat.");
    };

    public abstract String publicInfo();
    public abstract String privateInfo();

    public boolean isAdminOnly() {
        return ADMIN_ONLY;
    }

    /**
     * Does this user, with this guild, have the authority to run this command as system?
     * @param guild The guild where the command would be run at
     * @param user The user running this command
     * @return Whether the user could run this command as system
     */
    public boolean wouldAuthorize(Guild guild, User user) {
        if (guild != null) {
            Member member = guild.getMember(user);
            return member.getRoles().contains(bot.getSystemRole(guild));
        } else {
            return bot.hasSystemRoleEverywhere(user);
        }
    }
}
