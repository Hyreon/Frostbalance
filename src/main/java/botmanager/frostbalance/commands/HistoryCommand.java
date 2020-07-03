package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.frostbalance.history.RegimeData;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Collections;
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
        String result = "";
        boolean found = false;
        int page = 1;
        List<RegimeData> records;

        if (!(genericEvent instanceof GuildMessageReceivedEvent)) {
            return;
        }

        event = (GuildMessageReceivedEvent) genericEvent;
        message = event.getMessage().getContentRaw();

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

        if (words.length >= 1 && !message.isEmpty()) {
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

        records = bot.readRecords(event.getGuild());
        Collections.reverse(records);

        if (page > maxPages(records)) {

            if (page == 1) {
                result += "**This server has no history. This is likely an error, and you should inform staff.**";
            } else {
                result += "This number is too high, it must be at most " + maxPages(records) + ".";
            }
            Utilities.sendGuildMessage(event.getChannel(), result);
            return;
        }

        result += displayRecords(records, page);

        Utilities.sendGuildMessage(event.getChannel(), result);

    }

    private String displayRecords(List<RegimeData> records, int page) {

        List<RegimeData> sublist = records.subList((page - 1) * HISTORY_PAGE_SIZE, Math.min(page * HISTORY_PAGE_SIZE, records.size()));
        List<String> displayList = sublist.stream().map(regimeData -> regimeData.forHumans(bot.getJDA())).collect(Collectors.toList());

        return String.join("\n", displayList)
                + "\nPage " + page + "/" + maxPages(records);

    }

    private int maxPages(List<?> list) {
        return (int) Math.ceil(list.size() / (double) HISTORY_PAGE_SIZE);
    }

    @Override
    public String info() {
        return "**" + bot.getPrefix() + "history PAGE** - find out about previous regimes.";
    }

}