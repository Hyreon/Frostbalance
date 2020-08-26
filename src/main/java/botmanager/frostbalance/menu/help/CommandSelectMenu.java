package botmanager.frostbalance.menu.help;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.CommandContext;
import botmanager.frostbalance.command.FrostbalanceCommand;
import botmanager.frostbalance.menu.option.OptionMenu;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A menu that displays all commands by letting you pick between CommandTypeMenus to
 * display.
 */
public class CommandSelectMenu extends OptionMenu<FrostbalanceCommand> {

    public CommandSelectMenu(Frostbalance bot, CommandContext context, List<FrostbalanceCommand> commandsToShow) {
        super(bot, context, commandsToShow);
    }

    @Override
    protected void select(@NotNull FrostbalanceCommand command) {
        adopt(new CommandViewMenu(getBot(), getContext(), command), true);
    }
}
