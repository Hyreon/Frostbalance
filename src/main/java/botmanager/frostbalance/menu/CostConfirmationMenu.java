package botmanager.frostbalance.menu;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.GuildWrapper;
import botmanager.frostbalance.command.GuildMessageContext;
import botmanager.frostbalance.menu.response.MenuResponse;

//FIXME right now this gives away information about the players' influence level; how could this be fixed?
public class CostConfirmationMenu extends ConfirmationMenu {

    boolean sentInfluence = false;

    public CostConfirmationMenu(Frostbalance bot, GuildMessageContext context, Runnable runnable, String description, GuildWrapper guild) {
        super(bot, context, runnable, description);
        this.menuResponses.add(new MenuResponse("\uD83D\uDCAC", "DM Current Influence") {
            @Override
            public void reactEvent() {
                Utilities.sendPrivateMessage(getJdaActor(), "You have " + guild.getMember(getActor()).getInfluence() + " influence in " + guild);
                sentInfluence = true;
                updateMessage();
            }

            @Override
            public boolean isValid() {
                return !sentInfluence;
            }
        });
    }

}
