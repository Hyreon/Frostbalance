package botmanager.frostbalance.grid;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Nation;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PlayerCharacter extends TileObject {

    static List<PlayerCharacter> cache = new ArrayList<>();

    public static PlayerCharacter get(User user, WorldMap map) {
        for (PlayerCharacter player : cache) {
            if (player.getUser().equals(user) && player.getMap().equals(map)) {
                return player;
            }
        }
        PlayerCharacter newPlayer = new PlayerCharacter(user, map);
        cache.add(newPlayer);
        System.out.println("Creating new character");
        return newPlayer;
    }

    public static PlayerCharacter get(User user, Guild guild) {
        return get(user, WorldMap.get(guild));
    }

    /**
     * The user this character is tied to.
     */
    User user;

    /**
     * The queued destination that the player is currently approaching via the fastest known route.
     */
    Hex destination;

    public PlayerCharacter(User user, WorldMap map) {
        super(map, new Hex());
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    /**
     *
     * @return The nation this player is a part of, if relevant.
     * Note the user can change this at any time.
     */
    public Nation getNation() {
        if (map.isMainMap()) {
            return Frostbalance.bot.getMainAllegiance(user);
        } else {
            return Nation.NONE;
        }
    }

    public void adjustDestination(Hex.Direction direction) {
        if (destination == null) destination = getLocation();
        destination = destination.move(direction);
        updateMovement();
    }

    /**
     * Right now pretty simple: go immediately to the target destination without any waiting or routing.
     */
    private void updateMovement() {
        setLocation(destination);
    }

    @Override
    public URL getRender() {
        try {
            return new URL(getUser().getEffectiveAvatarUrl());
        } catch (MalformedURLException e) {
            System.err.println("Effective avatar URL is malformed!");
            e.printStackTrace();
            return null;
        }
    }

    public Member getMember() {
        if (map.getGuild() == null) {
            Guild guild = Frostbalance.bot.getGuildFor(getNation());
            if (guild == null) {
                return null;
            } else {
                return guild.getMember(getUser());
            }
        } else {
            return map.getGuild().getMember(getUser());
        }
    }

    public String getName() {
        Member member = getMember();
        if (member == null) {
            return getUser().getName();
        } else {
            return member.getEffectiveName();
        }
    }
}
