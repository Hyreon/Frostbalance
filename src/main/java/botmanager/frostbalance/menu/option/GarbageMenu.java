package botmanager.frostbalance.menu.option;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.MessageContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GarbageMenu extends OptionMenu<Integer> {

    public GarbageMenu(@NotNull Frostbalance bot, @NotNull MessageContext context, @NotNull List<? extends Integer> options) {
        super(bot, context, options);
    }

    public GarbageMenu(@NotNull Frostbalance bot, @NotNull MessageContext context, @NotNull List<? extends Integer> options, int page) {
        super(bot, context, options, page);
    }

    @Override
    protected void select(@NotNull Integer option) {
        System.out.println(this + " is now creating a child.");
        List<Integer> range = IntStream.rangeClosed(1, 100).map(x -> x * option)
                .boxed().collect(Collectors.toList());
        redirectTo(new GarbageMenu(getBot(), getContext(), range), true);
        if (getOriginalMenu().getMessage() != null) {
            getOriginalMenu().getMessage().getChannel().sendMessage(option.toString() + " HAS NOW BEEN SAID!!").queue();
        }
    }
}
