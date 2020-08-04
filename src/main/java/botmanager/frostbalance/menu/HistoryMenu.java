package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.data.RegimeData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryMenu extends Menu {

    public static final int HISTORY_PAGE_SIZE = 6;

    List<RegimeData> regimeList;
    int page = 1; //1-indexed.

    Guild guildContext;

    public HistoryMenu(Frostbalance bot, List<RegimeData> regimeList, int page, Guild guildContext) {
        super(bot);
        this.regimeList = regimeList;
        this.page = page;

        this.guildContext = guildContext;

        menuResponses.add(new MenuResponse("⬅", "Previous") {

            @Override
            public void reactEvent() {
                previousPage();
            }

            @Override
            public boolean validConditions() {
                return getPage() > 1;
            }

        });

        menuResponses.add(new MenuResponse("➡", "Next") {

            @Override
            public void reactEvent() {
                nextPage();
            }

            @Override
            public boolean validConditions() {
                return getPage() < maxPages();
            }

        });

        menuResponses.add(new MenuResponse("✅", "Exit") {

            @Override
            public void reactEvent() {
                close(true);
            }

            @Override
            public boolean validConditions() {
                return true;
            }

        });

        menuResponses.add(new MenuResponse("\uD83D\uDCCC", "Display") {

            @Override
            public void reactEvent() {
                close(false);
            }

            @Override
            public boolean validConditions() {
                return true;
            }

        });
    }

    private int getPage() {
        return page;
    }

    private void previousPage() {
        page--;
        updateMessage();
    }

    private void nextPage() {
        page++;
        updateMessage();
    }

    public int maxPages() {
        return (int) Math.ceil(regimeList.size() / (double) HISTORY_PAGE_SIZE);
    }

    @Override
    public EmbedBuilder getMEBuilder() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (closed) {
            embedBuilder.setColor(Color.DARK_GRAY);
        } else {
            embedBuilder.setColor(bot.getGuildColor(guildContext));
        }
        embedBuilder.setTitle("\nPage " + page + "/" + maxPages());
        embedBuilder.setDescription(displayRecords());
        return embedBuilder;
    }

    private String displayRecords() {

        List<RegimeData> sublist = regimeList.subList((page - 1) * HISTORY_PAGE_SIZE, Math.min(page * HISTORY_PAGE_SIZE, regimeList.size()));
        List<String> displayList = sublist.stream().map(regimeData -> regimeData.forHumans()).collect(Collectors.toList());

        return String.join("\n", displayList);

    }

}
