package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.GuildWrapper;
import botmanager.frostbalance.OldOptionFlag;
import botmanager.frostbalance.command.AuthorityLevel;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class FlagMenu extends Menu {

    /**
     * If true, the flagMenu is turning ON certain flags, rather than turning them off.
     */
    boolean enabling = true;
    GuildWrapper bGuild;

    public FlagMenu(Frostbalance bot, GuildWrapper guild) {
        super(bot);
        this.bGuild = guild;

        for (OldOptionFlag flag : OldOptionFlag.values()) {
            menuResponses.add(new MenuResponse(flag.getEmoji(), flag.getLabel()) {

                @Override
                public void reactEvent() {
                    bot.getGuildWrapper(bGuild.getId()).flipFlag(flag);

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
                return !enabling && getActor().memberIn(bGuild).hasAuthority(AuthorityLevel.GUILD_ADMIN);
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
                return enabling && getActor().memberIn(bGuild).hasAuthority(AuthorityLevel.GUILD_ADMIN);
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

    private boolean isToggleable(OldOptionFlag flag) {
        return (bGuild.hasFlag(flag) ^ enabling) && getActor().memberIn(bGuild).hasAuthority(flag.getAuthorityToChange());
    }

    @Override
    public EmbedBuilder getMEBuilder() {
        EmbedBuilder builder = new EmbedBuilder();
        if (closed) {
            builder.setColor(Color.DARK_GRAY);
        } else {
            builder.setColor(bGuild.getColor());
        }
        builder.setTitle(bGuild.getName() + ": Flags");
        String flagsEnabled = "";
        for (OldOptionFlag flag : bGuild.getOldOptionFlags()) {
            flagsEnabled += flag.getEmoji() + " " + flag.getLabel() + "\n";
        }
        builder.setDescription(flagsEnabled);
        return builder;
    }
}
