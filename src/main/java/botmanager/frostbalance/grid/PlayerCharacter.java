package botmanager.frostbalance.grid;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Nation;
import botmanager.frostbalance.Player;
import botmanager.Utilities;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.action.Action;
import botmanager.frostbalance.checks.FrostbalanceException;
import botmanager.frostbalance.grid.coordinate.Hex;
import botmanager.frostbalance.resource.Inventory;
import net.dv8tion.jda.api.entities.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerCharacter extends Mobile {

    public Inventory inventory;
    transient Queue<Action> actionQueue = new PriorityQueue<>();

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
    public UserWrapper getUser() {
        return Frostbalance.bot.getUserWrapper(userId);
    }

    public String getUserId() {
        return userId;
    }

    public Queue<Action> getActionQueue() {
        if (actionQueue == null) actionQueue = new PriorityQueue<>();
        return actionQueue;
    }

    public void doNextAction() {
        Action action = getActionQueue().poll();
        if (action == null) return;
        try {
            action.doAction(this);
        } catch (FrostbalanceException e) {
            User jdaUser = getUser().getJdaUser();
            if (jdaUser != null) {
                Utilities.sendPrivateMessage(getUser().getJdaUser(), "Could not perform " + action.getClass().getSimpleName() + ":\n" +
                        String.join("\n", e.displayCauses()));
            }
        }
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
                URL url = new URL(getUser().getJdaUser().getEffectiveAvatarUrl());
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

    public Player getPlayer() {
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

    public Inventory getInventory() {
        if (inventory == null) inventory = new Inventory();
        return inventory;
    }

}
