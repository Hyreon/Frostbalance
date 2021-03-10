package botmanager.frostbalance.grid;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Nation;
import botmanager.frostbalance.Player;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.action.ActionQueue;
import botmanager.frostbalance.action.QueueStep;
import botmanager.frostbalance.action.actions.Action;
import botmanager.frostbalance.action.actions.WorkAction;
import botmanager.frostbalance.action.routine.MoveToRoutine;
import botmanager.frostbalance.action.routine.RepeatRoutine;
import botmanager.frostbalance.action.routine.SearchRoutine;
import botmanager.frostbalance.checks.FrostbalanceException;
import botmanager.frostbalance.grid.coordinate.Hex;
import botmanager.frostbalance.resource.Inventory;
import botmanager.utils.JDAUtils;
import net.dv8tion.jda.api.entities.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerCharacter extends Mobile {

    public Inventory inventory;
    transient ActionQueue actionQueue = new ActionQueue(this);
    private transient double moves = 0.0;

    /**
     * The user this character is tied to.
     */
    String userId;

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

    public ActionQueue getActionQueue() {
        if (actionQueue == null) actionQueue = new ActionQueue(this);
        return actionQueue;
    }

    public boolean doNextAction() {
        System.out.println("Doing actions for " + getName());
        List<QueueStep> stepsPerformed = new LinkedList<>();
        int spentMoves = 0;
        for(;;) {
            QueueStep base = getActionQueue().peekBase();
            Action action = getActionQueue().poll();
            if (action != null //is supposed to do something
                    && moves >= action.moveCost() //character has the energy to do the thing
                    && spentMoves < 1 //character hasn't already moved the 1-tile limit
                    && (action.moveCost() > 0 || !stepsPerformed.contains(base))) { //not a repeat of a zero-cost task (no infinite loops); beats both bad Routines and ActionQueues
                try {
                    spentMoves += action.moveCost();
                    moves -= action.moveCost();
                    action.doAction();
                    System.out.println("Did " + action.getClass().getSimpleName());
                } catch (FrostbalanceException e) {
                    User jdaUser = getUser().getJdaUser();
                    if (jdaUser != null) {
                        JDAUtils.sendPrivateMessage(getUser().getJdaUser(), "Could not perform " + action.getClass().getSimpleName() + ":\n" +
                                String.join("\n", e.displayCauses()));
                    }
                }
                if (getPlayer().getUserWrapper().getUserOptions().getLoopActions() && base != null) {
                    stepsPerformed.add(base);
                }
            } else {
                for (QueueStep step : stepsPerformed) {
                    if (step.equals(getActionQueue().peekBase())) continue; //shh, still in progress
                    getActionQueue().add(step.refreshed());
                }
                if (action == null) { moves = 0.0; }
                break;
            }
        }
        if (spentMoves >= 1) moves = 0.0; //reset available moves if a move was successful
        return true;
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
        getActionQueue().add(new MoveToRoutine(this, destination));
    }

    public void adjustDestination(Hex.Direction direction, int amount) {
        QueueStep previousRoutine = getActionQueue().peekLast();
        Hex previousDestination;
        if (previousRoutine instanceof MoveToRoutine) {
            System.out.println("Replacing last destination!");
            List<Hex> softWaypoints = ((MoveToRoutine) previousRoutine).readSoftWaypoints();
            previousDestination = ((MoveToRoutine) previousRoutine).getDestination();
            actionQueue.pollLast();
            actionQueue.add(new MoveToRoutine(getActionQueue(), softWaypoints, previousDestination, direction, amount));
        } else {
            List<Hex> hardWaypoints = actionQueue.simulation().waypoints(false);
            if (hardWaypoints.size() == 0) {
                System.out.println("Creating first destination!");
                actionQueue.add(new MoveToRoutine(getActionQueue(), direction, amount));
            } else {
                System.out.println("Adding after last destination!");
                previousDestination = hardWaypoints.get(hardWaypoints.size() - 1); //last waypoint
                actionQueue.add(new MoveToRoutine(getActionQueue(), previousDestination, direction, amount));
            }
        }
    }

    public void searchTile(int itemsToFind) {
        int targetItems = getMap().getTile(getDestination()).getResourceData().numResources() + itemsToFind;
        getActionQueue().add(new SearchRoutine(this, targetItems));
    }

    public void work(int workCycles) {
        getActionQueue().add(new RepeatRoutine<>(new WorkAction(this), workCycles));
    }

    public Hex getDestination() {
        List<Hex> waypoints = getActionQueue().simulation().waypoints(false);
        return waypoints.get(waypoints.size() - 1); //last waypoint
    }

    @Override
    public InputStream getRender() {
        try {
            if (getUser() != null && getUser().getJdaUser() != null) { //user is accessible
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


    @Override
    public boolean turnAction() {
        moves += 1.0; //register that a turn has passed, and more movement is available.
        return doNextAction();
    }

    public String getTravelTime() {
        return getLocation().minimumDistance(getDestination()) * 4 + " minutes";
    }

    public Inventory getInventory() {
        if (inventory == null) inventory = new Inventory();
        return inventory;
    }

    public void adopt() {
        getActionQueue().setParent(this);
        getActionQueue().adopt();
    }
}
