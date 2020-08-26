package botmanager.frostbalance.menu.option;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.CommandContext;
import botmanager.frostbalance.menu.response.MenuResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class OptionMenu<T> extends ListMenu<T> {

    protected List<MenuResponse> pickResponses = new ArrayList<>();

    public OptionMenu(@NotNull Frostbalance bot, @NotNull CommandContext context, @NotNull List<? extends T> options) {
        super(bot, context, options);
        addOptions();
    }

    public OptionMenu(@NotNull Frostbalance bot, @NotNull CommandContext context, @NotNull List<? extends T> options, int page) {
        super(bot, context, options, page);
        addOptions();
    }

    private void addOptions() {
        for (int i = 1; i <= 10; i++) {
            final int index = i;
            MenuResponse responseForIndex = new MenuResponse(i == 10 ? "\uD83D\uDD1F" : i + "️⃣", "???") {

                @Override
                public void reactEvent() {
                    select(getSublist().get(index - 1));
                }

                @Override
                public boolean isValid() {
                    return getSublist().size() >= index;
                }
            };
            menuResponses.add(i - 1, responseForIndex);
            pickResponses.add(responseForIndex);
        }
    }

    public void updatePickResponseText() {
        for (int i = 0; i < getSublist().size(); i++) {
            pickResponses.get(i).setName(getSublist().get(i).toString());
        }
    }

    @Override
    public EmbedBuilder getEmbedBuilder() {
        if (isClosed()) {
            return super.getEmbedBuilder();
        } else {
            updatePickResponseText();
            return super.getEmbedBuilder().setDescription(null);
        }
    }

    protected abstract void select(@NotNull T command);
}
