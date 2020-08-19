package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.OldOptionFlag;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.GuildCommandContext;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class FlagMenu extends Menu {

    /**
     * If true, the flagMenu is turning ON certain flags, rather than turning them off.
     */
    boolean enabling = true;

    public FlagMenu(Frostbalance bot, GuildCommandContext context) {
        super(bot, context);

        for (OldOptionFlag flag : OldOptionFlag.values()) {
            menuResponses.add(new MenuResponse(flag.getEmoji(), flag.getLabel()) {

                @Override
                public void reactEvent() {
                    bot.getGuildWrapper(getContext().getGuild().getId()).flipFlag(flag);

                    updateMessage();
                }

                @Override
                public boolean isValid() {
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
            public boolean isValid() {
                return !enabling && getActor().memberIn(getContext().getGuild()).hasAuthority(AuthorityLevel.GUILD_ADMIN);
            }
        });

        menuResponses.add(new MenuResponse("\uD83D\uDCE4", "Switch to disabling") {

            @Override
            public void reactEvent() {
                enabling = false;
                updateMessage();
            }

            @Override
            public boolean isValid() {
                return enabling && getActor().memberIn(getContext().getGuild()).hasAuthority(AuthorityLevel.GUILD_ADMIN);
            }
        });

        menuResponses.add(new MenuResponse("❎", "Exit") {

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

    private boolean isToggleable(OldOptionFlag flag) {
        return (getContext().getGuild().hasFlag(flag) ^ enabling) && getActor().memberIn(getContext().getGuild()).hasAuthority(flag.getAuthorityToChange());
    }

    @Override
    public EmbedBuilder getEmbedBuilder() {
        EmbedBuilder builder = new EmbedBuilder();
        if (isClosed()) {
            builder.setColor(Color.DARK_GRAY);
        } else {
            builder.setColor(getContext().getGuild().getColor());
        }
        builder.setTitle(getContext().getGuild().getName() + ": Flags");
        String flagsEnabled = "";
        for (OldOptionFlag flag : getContext().getGuild().getOldOptionFlags()) {
            flagsEnabled += flag.getEmoji() + " " + flag.getLabel() + "\n";
        }
        builder.setDescription(flagsEnabled);
        return builder;
    }
}
