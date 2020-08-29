package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.MessageContext;
import botmanager.frostbalance.records.RegimeData;
import botmanager.frostbalance.menu.option.ListMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HistoryMenu extends ListMenu<RegimeData> {

    public static final int HISTORY_PAGE_SIZE = 6;

    public HistoryMenu(Frostbalance bot, MessageContext context, List<RegimeData> regimes, int page) {
        super(bot, context, regimes, page);
        setPageSize(HISTORY_PAGE_SIZE);
    }

    @Override
    public @NotNull EmbedBuilder getEmbedBuilder() {
        return super.getEmbedBuilder().setColor(getContext().getGuild().getColor());
    }

}
