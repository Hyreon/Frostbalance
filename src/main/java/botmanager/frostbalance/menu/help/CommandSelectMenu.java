package botmanager.frostbalance.menu.help;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.CommandContext;
import botmanager.frostbalance.menu.Menu;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * A menu that displays all commands by letting you pick between CommandTypeMenus to
 * display.
 */
public class CommandSelectMenu extends Menu {

    public CommandSelectMenu(Frostbalance bot, CommandContext context) {
        super(bot, context);
    }

    @Override
    public EmbedBuilder getEmbedBuilder() {
        return null;
    }
}
