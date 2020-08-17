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

        for (Nation nation : Nation.getNations()) {

            menuResponses.add(new MenuResponse(nation.getEmoji(), nation.toString()) {

                @Override
                public void reactEvent() {
                    getActor().playerIn(bot.getMainNetwork()).setAllegiance(nation);
                    close(false);
                }

                @Override
                public boolean validConditions() {
                    return true;
                }
            });

        }

        menuResponses.add(new MenuResponse("✖️", "Don't change for now") {

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
                builder.setDescription("You will now claim tiles in the name of " + getActor().playerIn(bot.getMainNetwork()).getAllegiance() + ". You may now make claims.");
            } else if (cause == Cause.CHANGE) {
                builder.setDescription("Your allegiance has been moved to " + getActor().playerIn(bot.getMainNetwork()).getAllegiance());
            }
            builder.setColor(getActor().playerIn(bot.getMainNetwork()).getAllegiance().getColor());
        } else {
            builder.setTitle("Set allegiance");
            if (cause == Cause.NOT_SET) {
                builder.setDescription("This claim cannot be made. In order to claim tiles, you must first set your allegiance." + ADDENDUM);
                builder.setColor(Color.GRAY);
            } else if (cause == Cause.CHANGE) {
                builder.setDescription("Pick your new allegiance. Current allegiance: " + getActor().playerIn(bot.getMainNetwork()).getAllegiance() + ADDENDUM);
                builder.setColor(getActor().playerIn(bot.getMainNetwork()).getAllegiance().getColor());
            }
        }
        return builder;
    }

    public enum Cause {
        NOT_SET, CHANGE
    }

}
