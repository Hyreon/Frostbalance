package botmanager.frostbalance.commands.meta;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.CommandContext;
import botmanager.frostbalance.command.FrostbalanceCommandBase;
import botmanager.frostbalance.menu.GarbageMenu;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GarbageCommand extends FrostbalanceCommandBase {

    public GarbageCommand(Frostbalance bot) {
        super(bot, new String[] {
                "garbage"
        }, AuthorityLevel.GENERIC);
    }

    @Override
    protected void execute(CommandContext context, String[] params) {
        List<Integer> range = IntStream.rangeClosed(1, 100)
                .boxed().collect(Collectors.toList());
        new GarbageMenu(bot, context, range).send(context.getChannel(), context.getAuthor());
    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "Do some garbage with .garbage";
    }
}
