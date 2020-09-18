package botmanager.frostbalance.menu.settings;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.MessageContext;
import botmanager.frostbalance.command.GuildMessageContext;
import botmanager.frostbalance.menu.Menu;
import botmanager.frostbalance.menu.response.MenuResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

public class ParentSettingsMenu extends Menu {

    /**
     * If true, the flagMenu is turning ON certain flags, rather than turning them off.
     */
    boolean enabling = true;

    public ParentSettingsMenu(Frostbalance bot, MessageContext context) {
        //BOT-WIDE
        //NETWORK-WIDE (must be in a network AND have at least network-admin)
        //NATION-WIDE (must be in a nation AND have at least nation-admin)
        //PERSONAL (always available; clicked immediately if this is the only one available)
        super(bot, context);

        menuResponses.add(new MenuResponse("\uD83C\uDF10", "Bot Settings") {

            @Override
            public void reactEvent() {
                redirectTo(new BotSettingsMenu(bot, context), true);
            }

            @Override
            public boolean isValid() {
                return getActor().memberIn(getContext().getGuild()).hasAuthority(AuthorityLevel.BOT_ADMIN);
            }
        });

        menuResponses.add(new MenuResponse("\uD83D\uDDFA", "Network Settings") {

            @Override
            public void reactEvent() {
                redirectTo(new NetworkSettingsMenu(bot, new GuildMessageContext(context)), true);
            }

            @Override
            public boolean isValid() {
                return getActor().memberIn(getContext().getGuild()).hasAuthority(AuthorityLevel.MAP_ADMIN);
            }
        });

        menuResponses.add(new MenuResponse("\uD83C\uDFF3", "Nation Settings") {

            @Override
            public void reactEvent() {
                redirectTo(new NationSettingsMenu(bot, new GuildMessageContext(context)), true);
            }

            @Override
            public boolean isValid() {
                return getActor().memberIn(getContext().getGuild()).hasAuthority(AuthorityLevel.NATION_LEADER);
            }
        });

        menuResponses.add(new MenuResponse("\uD83D\uDC64", "Personal Settings") {

            @Override
            public void reactEvent() {
                redirectTo(new UserSettingsMenu(bot, context), true);
            }

            @Override
            public boolean isValid() {
                return getActor().memberIn(getContext().getGuild()).hasAuthority(AuthorityLevel.GENERIC);
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

    @NotNull
    @Override
    public EmbedBuilder getEmbedBuilder() {
        return new EmbedBuilder()
                .setTitle(":gear: Settings")
                .setDescription("Choose the settings you wish to change");
    }
}
