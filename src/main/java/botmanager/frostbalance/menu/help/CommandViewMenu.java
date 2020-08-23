package botmanager.frostbalance.menu.help;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.CommandContext;
import botmanager.frostbalance.command.FrostbalanceCommand;
import botmanager.frostbalance.menu.Menu;
import net.dv8tion.jda.api.EmbedBuilder;

public class CommandViewMenu extends Menu {

    FrostbalanceCommand command;

    public CommandViewMenu(Frostbalance bot, CommandContext context, FrostbalanceCommand command) {
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
