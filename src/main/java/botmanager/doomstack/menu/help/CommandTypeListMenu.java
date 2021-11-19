package botmanager.doomstack.menu.help;

import botmanager.doomstack.DoomStack;
import botmanager.doomstack.menu.Menu;
import botmanager.doomstack.MessageContext;
import net.dv8tion.jda.api.EmbedBuilder;

public class CommandTypeListMenu extends Menu {

    public CommandTypeListMenu(DoomStack bot, MessageContext context) {
        super(bot, context);
    }

    @Override
    public EmbedBuilder getEmbedBuilder() {
        return null;
    }
}
