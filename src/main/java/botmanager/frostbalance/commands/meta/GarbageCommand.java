package botmanager.frostbalance.commands.meta;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.MessageContext;
import botmanager.frostbalance.command.ContextLevel;
import botmanager.frostbalance.command.FrostbalanceCommand;
import botmanager.frostbalance.menu.option.GarbageMenu;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GarbageCommand extends FrostbalanceCommand {

    public GarbageCommand(Frostbalance bot) {
        super(bot, new String[] {
                "garbage"
        }, AuthorityLevel.GENERIC, ContextLevel.ANY);
    }

    @Override
    protected void execute(MessageContext context, String[] params) {
        List<Integer> range = IntStream.rangeClosed(1, 100)
                .boxed().collect(Collectors.toList());
        new GarbageMenu(getBot(), context, range).send(context.getChannel(), context.getAuthor());
    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "Do some garbage with .garbage";
    }
}
