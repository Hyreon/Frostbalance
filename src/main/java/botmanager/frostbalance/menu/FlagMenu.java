package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.OptionFlag;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.awt.*;

public class FlagMenu extends Menu {

    /**
     * If true, the flagMenu is turning ON certain flags, rather than turning them off.
     */
    boolean enabling = true;
    Guild guild;

    public FlagMenu(Frostbalance bot, Guild guild) {
        super(bot);
        this.guild = guild;

        for (OptionFlag flag : OptionFlag.values()) {
            menuResponses.add(new MenuResponse(flag.getEmoji(), flag.getLabel()) {

                @Override
                public void reactEvent() {
                    bot.flipFlag(guild, flag);

                    updateMessage();
                }

                @Override
                public boolean validConditions() {
                    return isToggleable(flag);
                }
            });
        }

        menuResponses.add(new MenuResponse("\uD83D\uDCE5", "Switch to enabling") {

            @Override
            public void reactEvent() {
                enabling = true;
                updateMessage();
            }

            @Override
            public boolean validConditions() {
                return !enabling;
            }
        });

        menuResponses.add(new MenuResponse("\uD83D\uDCE4", "Switch to disabling") {

            @Override
            public void reactEvent() {
                enabling = false;
                updateMessage();
            }

            @Override
            public boolean validConditions() {
                return enabling;
            }
        });

        menuResponses.add(new MenuResponse("❎", "Exit") {

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

    private boolean isToggleable(OptionFlag flag) {
        return (bot.getDebugFlags(guild).contains(flag) ^ enabling) && bot.getAuthority(guild, getActor()).hasAuthority(flag.getAuthorityToChange());
    }

    @Override
    public EmbedBuilder getMEBuilder() {
        EmbedBuilder builder = new EmbedBuilder();
        if (closed) {
            builder.setColor(Color.DARK_GRAY);
        } else {
            builder.setColor(bot.getGuildColor(guild));
        }
        builder.setTitle("Flags");
        String flagsEnabled = "";
        for (OptionFlag flag : bot.getDebugFlags(guild)) {
            flagsEnabled += flag.getEmoji() + " " + flag.getLabel() + "\n";
        }
        builder.setDescription(flagsEnabled);
        return builder;
    }
}
