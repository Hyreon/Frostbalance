package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.frostbalance.history.TerminationCondition;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;

public class TransferCommand extends FrostbalanceCommandBase {

    final String[] KEYWORDS = {
            bot.getPrefix() + "transfer"
    };

    public TransferCommand(BotBase bot) {
        super(bot);
    }

    @Override
    public void run(Event genericEvent) {

        GuildMessageReceivedEvent event;
        String message;
        String id;
        String result;
        Member currentOwner;
        boolean found = false;

        if (!(genericEvent instanceof GuildMessageReceivedEvent)) {
            return;
        }

        event = (GuildMessageReceivedEvent) genericEvent;
        message = event.getMessage().getContentRaw();
        id = event.getAuthor().getId();

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

        try {
            String info = Utilities.read(new File("data/" + bot.getName() + "/" + event.getGuild().getId() + "/owner.csv"));
            currentOwner = event.getGuild().getMember(event.getJDA().getUserById(Utilities.getCSVValueAtIndex(info, 0)));
        } catch (NullPointerException | IllegalArgumentException e) {
            currentOwner = null;
        }

        Member member = event.getGuild().getMemberById(id);

        if (!member.equals(currentOwner)) {
            result = "You have to be the owner to transfer ownership of the server peaceably.";

            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        if (message.isEmpty()) {
            result = info();
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        id = Utilities.findUserId(event.getGuild(), message);

        if (id == null) {
            result = "Couldn't find user '" + message + "'.";
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        if (event.getGuild().getMemberById(id).getRoles().contains(bot.getSystemRole(event.getGuild()))) {
            result = "A very generous offer, but I can't accept.";
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        User newOwner = event.getJDA().getUserById(id);

        bot.endRegime(event.getGuild(), TerminationCondition.TRANSFER);
        bot.startRegime(event.getGuild(), newOwner);

        result = "**" + currentOwner.getEffectiveName() + "** has transferred ownership to " +
                newOwner.getAsMention() + " for this server.";
        Utilities.sendGuildMessage(event.getChannel(), result);

    }

    @Override
    public String info() {
        return ""
                + "**" + bot.getPrefix() + "transfer USER** - makes someone else server owner.";
    }

}
