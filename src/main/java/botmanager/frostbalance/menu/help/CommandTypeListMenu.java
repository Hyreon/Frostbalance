package botmanager.frostbalance.menu.help;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.CommandContext;
import botmanager.frostbalance.menu.Menu;
import net.dv8tion.jda.api.EmbedBuilder;

public class CommandTypeListMenu extends Menu {

    public CommandTypeListMenu(Frostbalance bot, CommandContext context) {
        super(bot, context);
    }

    @Override
    public EmbedBuilder getEmbedBuilder() {
        return null;
    }
}
