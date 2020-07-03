package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.frostbalance.history.RegimeData;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;
import java.util.stream.Collectors;

public class HistoryCommand extends FrostbalanceCommandBase {

    private static final int HISTORY_PAGE_SIZE = 5;

    final String[] KEYWORDS = {
            bot.getPrefix() + "history"
    };

    public HistoryCommand(BotBase bot) {
        super(bot);
    }

    @Override
    public void run(Event genericEvent) {
        GuildMessageReceivedEvent event;
        String[] words;
        String message;
        String id;
        String result = "";
        boolean found = false;
        int page = 1;
        List<RegimeData> records;

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

        words = message.split(" ");

        if (words.length >= 1) {
            try {
                page = Integer.parseInt(words[0]);
            } catch (NumberFormatException e) {
                result += "Couldn't recognize the number '" + words[0] + "'.";

                Utilities.sendGuildMessage(event.getChannel(), result);

                return;
            }
        }

        if (page < 0) {
            result += "This number is too low, it must be at least 1.";

            Utilities.sendGuildMessage(event.getChannel(), result);

            return;
        }

        records = bot.getRecords(event.getGuild());

        if (page > Math.ceil(maxPages(records))) {
            result += "This number is too high, it must be at most " + Math.ceil(maxPages(records)) + ".";

            Utilities.sendGuildMessage(event.getChannel(), result);

            return;
        }

        result += displayRecords(records, page);

        Utilities.sendGuildMessage(event.getChannel(), result);

    }

    private String displayRecords(List<RegimeData> records, int page) {

        List<RegimeData> sublist = records.subList((page - 1) * HISTORY_PAGE_SIZE, page * HISTORY_PAGE_SIZE);
        List<String> displayList = sublist.stream().map(regimeData -> regimeData.toString()).collect(Collectors.toList());

        return String.join("\n", displayList)
                + " Page " + page + "/" + maxPages(records);

    }

    private int maxPages(List<?> list) {
        return (int) Math.ceil(list.size() / HISTORY_PAGE_SIZE);
    }

    @Override
    public String info() {
        return "**" + bot.getPrefix() + "history PAGE** - find out about previous regimes.";
    }

}
