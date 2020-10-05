package botmanager.frostbalance.grid;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Nation;
import botmanager.frostbalance.Player;
import botmanager.frostbalance.grid.coordinate.Hex;
import botmanager.frostbalance.resource.Inventory;
import net.dv8tion.jda.api.entities.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerCharacter extends Mobile {

    public Inventory inventory;

    /**
     * The user this character is tied to.
     */
    String userId;

    /**
     * The queued destination that the player is currently approaching via the fastest known route.
     */
    Hex destination;

    public PlayerCharacter(String userId, WorldMap map) {
        super(map.getTile(Hex.origin()));
        this.userId = userId;
    }

    /**
     * @return The user from this id, or null if the user is inaccessible.
     */
    public User getUser() {
        User user = Frostbalance.bot.getJDA().getUserById(userId);
        return user;
    }

    public String getUserId() {
        return userId;
    }

    /**
     *
     * @return The nation this player is a part of, if relevant.
     * Note the user can change this at any time.
     */
    public Nation getNation() {
        return getPlayer().getAllegiance();
    }


    public void setDestination(Hex destination) {
        this.destination = destination;
        updateMovement();
    }

    public void adjustDestination(Hex.Direction direction, int amount) {
        destination = getDestination().move(direction, amount);
        updateMovement();
    }

    /**
     * Moves towards the destination in one second.
     */
    private boolean updateMovement() {

        if (!getLocation().equals(getDestination())) {

            Hex directions = destination.subtract(getLocation());
            Hex.Direction nextStep = directions.crawlDirection();
            setLocation(getLocation().move(nextStep));
            System.out.printf("%s now at %s\n", getName(), getLocation());
            return true;

        }

        return false;

    }

    public Hex getDestination() {
        if (destination == null) destination = getLocation();
        if (!destination.equals(getLocation())) updateMovement();
        return destination;
    }

    @Override
    public InputStream getRender() {
        try {
            if (getUser() != null) { //user is accessible
                URL url = new URL(getUser().getEffectiveAvatarUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "");
                return connection.getInputStream();
            }
            else return null;
        } catch (MalformedURLException e) {
            System.err.println("Effective avatar URL is malformed!");
            e.printStackTrace();
            return null;
        } catch (IOException ex) {
            Logger.getLogger(PlayerCharacter.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private Player getPlayer() {
        return Frostbalance.bot.getUserWrapper(userId).playerIn(getMap().getGameNetwork());
    }

    public String getName() {
        return getPlayer().getName();
    }

    public String getTravelTime() {
        return getLocation().minimumDistance(getDestination()) * 4 + " minutes";
    }

    @Override
    public boolean turnAction() {
        return updateMovement();
    }
}
