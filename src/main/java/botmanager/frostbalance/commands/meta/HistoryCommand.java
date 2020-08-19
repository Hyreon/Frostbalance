package botmanager.frostbalance.commands.meta;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommandBase;
import botmanager.frostbalance.command.GuildCommandContext;
import botmanager.frostbalance.data.RegimeData;
import botmanager.frostbalance.menu.HistoryMenu;

import java.util.Collections;
import java.util.List;

public class HistoryCommand extends FrostbalanceGuildCommandBase {

    private static final int HISTORY_PAGE_SIZE = 6;

    public HistoryCommand(Frostbalance bot) {
        super(bot, new String[] {
                "history"
        }, AuthorityLevel.GENERIC);
    }

    @Override
    public void executeWithGuild(GuildCommandContext context, String... params) {
        String result = "";
        int page = 1;
        List<RegimeData> records;

        if (params.length >= 1) {
            try {
                page = Integer.parseInt(params[0]);
            } catch (NumberFormatException e) {
                result += "Couldn't recognize the number '" + params[0] + "'.";

                context.sendResponse(result);

                return;
            }
        }

        if (page < 0) {
            result += "This number is too low, it must be at least 1.";

            context.sendResponse(result);

            return;
        }

        records = context.getGuild().readRecords();
        Collections.reverse(records);

        if (page > maxPages(records)) {

            if (page == 1) {
                result += "**This server has no history.**";
            } else {
                result += "This number is too high, it must be at most " + maxPages(records) + ".";
            }

            context.sendResponse(result);
            return;
        }

        new HistoryMenu(bot, context, records, page).send(context.getChannel(), context.getAuthor());

    }

    @Override
    public String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + bot.getPrefix() + "history PAGE** - find out about previous regimes on a server.";
    }

    private int maxPages(List<?> list) {
        return (int) Math.ceil(list.size() / (double) HISTORY_PAGE_SIZE);
    }
}
