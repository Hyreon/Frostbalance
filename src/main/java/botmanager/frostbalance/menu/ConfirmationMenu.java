package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.MessageContext;
import botmanager.frostbalance.menu.response.MenuResponse;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class ConfirmationMenu extends Menu {

    private final Runnable action;

    private final String description;

    public ConfirmationMenu(Frostbalance bot, MessageContext context, Runnable runnable, String description) {
        super(bot, context);
        this.description = description;
        this.action = runnable;

        menuResponses.add(new MenuResponse("✅", "Confirm") {

            @Override
            public void reactEvent() {
                try {
                    action.run();
                    close(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    close(false);
                }
            }

            @Override
            public boolean isValid() {
                return true;
            }
        });

        menuResponses.add(new MenuResponse("❎", "Cancel") {

            @Override
            public void reactEvent() {
                close(true);
            }

            @Override
            public boolean isValid() {
                return true;
            }
        });
    }

    @Override
    public EmbedBuilder getEmbedBuilder() {
        EmbedBuilder builder = new EmbedBuilder();
        if (isClosed()) {
            builder.setColor(Color.BLACK);
            builder.setTitle("Error");
            builder.setDescription("An internal error occurred running this command. Please notify the developers.");
        } else {
            //builder.setColor(guild.map(guild -> guild.getColor()).orElse(Color.GRAY));
            builder.setTitle("Confirm Action");
            builder.setDescription(description);
        }
        return builder;
    }

    /**
     * Sends a confirmation if the given condition is TRUE.
     * If the condition is FALSE, then it will not send a confirmation;
     * instead, it will simply run its assigned runnable.
     * @param condition
     */
    public void sendOnCondition(boolean condition) {
        if (condition) send();
        else action.run();
    }
}
