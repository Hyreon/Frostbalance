package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class ConfirmationMenu extends Menu {

    private final String description;

    public ConfirmationMenu(Frostbalance bot, Runnable runnable, String description) {
        super(bot);
        this.description = description;

        menuResponses.add(new MenuResponse("✅", "Confirm") {

            @Override
            public void reactEvent() {
                try {
                    runnable.run();
                    close(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    close(false);
                }
            }

            @Override
            public boolean validConditions() {
                return true;
            }
        });

        menuResponses.add(new MenuResponse("❎", "Cancel") {

            @Override
            public void reactEvent() {
                close(true);
            }

            @Override
            public boolean validConditions() {
                return true;
            }
        });
    }

    @Override
    public EmbedBuilder getMEBuilder() {
        EmbedBuilder builder = new EmbedBuilder();
        if (closed) {
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
}