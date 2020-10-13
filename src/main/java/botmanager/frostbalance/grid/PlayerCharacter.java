package botmanager.frostbalance.grid;

import botmanager.Utilities;
import botmanager.Utils;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Nation;
import botmanager.frostbalance.Player;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.action.ActionQueue;
import botmanager.frostbalance.action.MoveAction;
import botmanager.frostbalance.action.QueueStep;
import botmanager.frostbalance.action.routine.MoveToRoutine;
import botmanager.frostbalance.action.routine.RepeatRoutine;
import botmanager.frostbalance.checks.FrostbalanceException;
import botmanager.frostbalance.grid.coordinate.Hex;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerCharacter extends TileObject {

    transient ActionQueue actionQueue = new ActionQueue();

    @Deprecated
    static List<PlayerCharacter> cache = new ArrayList<>();
    private transient double moves = 0.0;

    @Deprecated
    public static PlayerCharacter get(String userId, WorldMap map) {
        if (Utils.isNullOrEmpty(userId)) return null;
        for (PlayerCharacter character : cache) {
            if (character.getUserId().equals(userId) && character.getTile().getMap().equals(map)) {
                return character;
            }
        }
        PlayerCharacter newPlayer = new PlayerCharacter(userId, map);
        cache.add(newPlayer);
        System.out.println("Creating new character for user with id " + userId);
        return newPlayer;
    }

    @Deprecated
    public static PlayerCharacter get(User user, @NotNull Guild guild) {
        return get(user.getId(), WorldMap.get(guild));
    }

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
        if (actionQueue == null) actionQueue = new ActionQueue();
        return actionQueue;
    }

    public boolean doNextAction() {
        QueueStep action = getActionQueue().poll();
        while (action != null && moves >= action.moveCost()) {
            try {
                moves -= action.moveCost();
                action.doAction();
            } catch (FrostbalanceException e) {
                User jdaUser = getUser().getJdaUser();
                if (jdaUser != null) {
                    Utilities.sendPrivateMessage(getUser().getJdaUser(), "Could not perform " + action.getClass().getSimpleName() + ":\n" +
                            String.join("\n", e.displayCauses()));
                }
            }
            action = getActionQueue().poll();
        }
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
        getActionQueue().add(new RepeatRoutine(new MoveAction(this, direction), amount));
    }

    //TODO improve simulation so that this getDestination method doesn't directly reference the action or routines for movement.
    public Hex getDestination() {
        System.out.println("Getting destination");
        Hex destination = getLocation();
        ActionQueue simulation = getActionQueue().simulator();
        while (!simulation.isEmpty()) {
            //FIXME get the destination without freezing!!
            QueueStep step = simulation.pollBase();
            if (step instanceof MoveAction) {
                System.out.println("Moving once");
                destination = destination.move(((MoveAction) step).getDirection());
            } else if (step instanceof MoveToRoutine) {
                System.out.println("Moving to destination");
                destination = ((MoveToRoutine) step).getDestination();
            } else if (step instanceof RepeatRoutine && ((RepeatRoutine) step).getAction() instanceof MoveAction) {
                System.out.println("Moving n times: " + ((RepeatRoutine) step).getAmount());
                destination = destination.move(((MoveAction) ((RepeatRoutine) step).getAction()).getDirection(), ((RepeatRoutine) step).getAmount());
            }
        }
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

    @Override
    public boolean turnAction() {
        moves += 1.0; //register that a turn has passed, and more movement is available.
        return doNextAction();
    }

    public String getTravelTime() {
        return getLocation().minimumDistance(getDestination()) * 2 + " minutes";
    }
}
