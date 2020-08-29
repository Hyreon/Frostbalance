package botmanager.frostbalance.menu.settings;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.MessageContext;
import botmanager.frostbalance.menu.Menu;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

public class UserSettingsMenu extends Menu {
    public UserSettingsMenu(Frostbalance bot, MessageContext context) {
        super(bot, context);
    }

    @NotNull
    @Override
    public EmbedBuilder getEmbedBuilder() {
        return new EmbedBuilder()
                .setTitle("User Settings");
    }
}
