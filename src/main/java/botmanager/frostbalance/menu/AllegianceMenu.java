package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Nation;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class AllegianceMenu extends Menu {

    private static final String ADDENDUM = "\n*Note: If you change your allegiance, your claims will cease to be valid until you return to your original nation. You **cannot** move claims from one nation to another!*";
    Cause cause;

    public AllegianceMenu(Frostbalance bot, Cause cause) {
        super(bot);

        this.cause = cause;

        menuResponses.add(new MenuResponse("\uD83D\uDFE5", "Red") {

            @Override
            public void reactEvent() {
                bot.setMainAllegiance(getActor(), Nation.RED);
                close(false);
            }

            @Override
            public boolean validConditions() {
                return true;
            }
        });

        menuResponses.add(new MenuResponse("\uD83D\uDFE9", "Green") {

            @Override
            public void reactEvent() {
                bot.setMainAllegiance(getActor(), Nation.GREEN);
                close(false);
            }

            @Override
            public boolean validConditions() {
                return true;
            }
        });

        menuResponses.add(new MenuResponse("\uD83D\uDFE6", "Blue") {

            @Override
            public void reactEvent() {
                bot.setMainAllegiance(getActor(), Nation.BLUE);
                close(false);
            }

            @Override
            public boolean validConditions() {
                return true;
            }
        });

        menuResponses.add(new MenuResponse("✖️", "Don't set for now") {

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

    public AllegianceMenu(Frostbalance bot) {
        this(bot, Cause.NOT_SET);
    }

    @Override
    public EmbedBuilder getMEBuilder() {
        EmbedBuilder builder = new EmbedBuilder();
        if (isClosed()) {
            builder.setTitle("Allegiance set");
            if (cause == Cause.NOT_SET) {
                builder.setDescription("You will now claim tiles in the name of " + bot.getMainAllegiance(getActor()).getEffectiveName() + ". You may now make claims.");
            } else if (cause == Cause.CHANGE) {
                builder.setDescription("Your allegiance has been moved to " + bot.getMainAllegiance(getActor()).getEffectiveName());
            }
            builder.setColor(bot.getMainAllegiance(getActor()).getColor());
        } else {
            builder.setTitle("Set allegiance");
            if (cause == Cause.NOT_SET) {
                builder.setDescription("This claim cannot be made. In order to claim tiles, you must first set your allegiance." + ADDENDUM);
                builder.setColor(Color.GRAY);
            } else if (cause == Cause.CHANGE) {
                builder.setDescription("Pick your new allegiance. Current allegiance: " + bot.getMainAllegiance(getActor()).getEffectiveName() + ADDENDUM);
                builder.setColor(bot.getMainAllegiance(getActor()).getColor());
            }
        }
        return builder;
    }

    public enum Cause {
        NOT_SET, CHANGE;
    }

}
