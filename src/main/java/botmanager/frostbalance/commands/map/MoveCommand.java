package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.*;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.coordinate.Hex;
import botmanager.frostbalance.menu.input.ConfirmationMenu;

public class MoveCommand extends FrostbalanceGuildCommand {

    public MoveCommand(Frostbalance bot) {
        super(bot, new String[] {
                "move",
        }, AuthorityLevel.GENERIC, ContextLevel.ANY);
    }

    @Override
    protected void executeWithGuild(GuildMessageContext context, String... params) {

        PlayerCharacter character = context.getPlayer().getCharacter();

        ArgumentStream arguments = new ArgumentStream(params);
        Hex destination = arguments.nextCoordinate();
        if (destination == null) {
            context.sendResponse(getInfo(context));
            return;
        }

        if (character.getActionQueue().isEmpty()) {
            System.out.println("Empty queue, setting destination");
            character.setDestination(destination);
            System.out.println("Destination sent, displaying travel time");
            context.sendResponse(context.getMember().getEffectiveName() + " is now headed towards " + destination + ", and will arrive in "
                    + character.getTravelTime() + ".");
        } else {
            new ConfirmationMenu(getBot(), context, () -> {
                character.getActionQueue().clear();
                character.setDestination(destination);
                context.sendResponse(context.getMember().getEffectiveName() + " is now headed towards " + destination + ", and will arrive in "
                        + character.getTravelTime() + ".");
            }, "This will reset your previously planned actions.").send();
        }


    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + getBot().getPrefix() + "move LOCATION** - Queue your character to move to the destination";
    }
}
