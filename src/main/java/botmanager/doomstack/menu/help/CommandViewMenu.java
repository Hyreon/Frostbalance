package botmanager.doomstack.menu.help;

import botmanager.doomstack.DoomStack;
import botmanager.doomstack.MessageContext;
import botmanager.doomstack.menu.Menu;
import botmanager.frostbalance.command.FrostbalanceCommand;
import net.dv8tion.jda.api.EmbedBuilder;

public class CommandViewMenu extends Menu {

    FrostbalanceCommand command;

    public CommandViewMenu(DoomStack bot, MessageContext context, FrostbalanceCommand command) {
        super(bot, context);
        this.command = command;
    }

    @Override
    public EmbedBuilder getEmbedBuilder() {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(command.getMainAlias())
                .setDescription("Aliases: " + String.join(", ", command.getAlternativeAliases()));
        //for (CommandUsage usage : command.getUsages()) {
        //    embedBuilder.addField(usage.getSyntax(), usage.getEffect(), false);
        //}
        return embedBuilder;
    }
}
