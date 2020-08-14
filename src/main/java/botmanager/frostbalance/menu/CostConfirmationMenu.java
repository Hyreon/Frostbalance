package botmanager.frostbalance.menu;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.GuildWrapper;

//FIXME right now this gives away information about the players' influence level; how could this be fixed?
public class CostConfirmationMenu extends ConfirmationMenu {

    boolean sentInfluence = false;

    public CostConfirmationMenu(Frostbalance bot, Runnable runnable, String description, GuildWrapper guild) {
        super(bot, runnable, description);
        this.menuResponses.add(new MenuResponse("\uD83D\uDCAC", "DM Current Influence") {
            @Override
            public void reactEvent() {
                Utilities.sendPrivateMessage(getJdaActor(), "You have " + guild.getMember(actor).getInfluence() + " influence in " + guild);
                sentInfluence = true;
                updateMessage();
            }

            @Override
            public boolean validConditions() {
                return !sentInfluence;
            }
        });
    }

}
