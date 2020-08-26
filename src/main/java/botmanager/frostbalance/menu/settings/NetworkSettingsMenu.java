package botmanager.frostbalance.menu.settings;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.CommandContext;
import botmanager.frostbalance.menu.Menu;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

public class NetworkSettingsMenu extends Menu {
    public NetworkSettingsMenu(Frostbalance bot, CommandContext context) {
        super(bot, context);
    }

    @NotNull
    @Override
    public EmbedBuilder getEmbedBuilder() {
        return new EmbedBuilder()
                .setTitle("Network Settings");
    }
}
