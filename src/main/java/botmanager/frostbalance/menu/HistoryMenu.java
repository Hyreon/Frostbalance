package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.CommandContext;
import botmanager.frostbalance.data.RegimeData;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HistoryMenu extends ListMenu<RegimeData> {

    public static final int HISTORY_PAGE_SIZE = 6;

    public HistoryMenu(Frostbalance bot, CommandContext context, List<RegimeData> regimes, int page) {
        super(bot, context, regimes, page);
        setPageSize(HISTORY_PAGE_SIZE);
    }

    @Override
    public @NotNull EmbedBuilder getEmbedBuilder() {
        return super.getEmbedBuilder().setColor(getContext().getGuild().getColor());
    }

}
