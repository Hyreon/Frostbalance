package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Nation;
import botmanager.frostbalance.grid.Hex;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.Tile;
import botmanager.frostbalance.grid.WorldMap;
import botmanager.frostbalance.render.MapRenderer;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;

public class MapMenu extends Menu {

    private WorldMap map;
    private Hex.Direction lastDirection = null;

    private PlayerCharacter player;

    /**
     * The location of the camera when not snapping to the players' location.
     */
    private Hex cameraLocation;

    private CameraBehavior cameraBehavior = CameraBehavior.SNAP_TO_PLAYER;

    public MapMenu(Frostbalance bot, WorldMap map, PlayerCharacter player) {
        super(bot);
        this.map = map;
        this.player = player;

        menuResponses.add(new MenuResponse("\uD83D\uDD02", "Last Direction") {

            @Override
            public void reactEvent() {
                move();
                updateMessage();
            }

            @Override
            public boolean validConditions() {
                return lastDirection != null;
            }
        });

        menuResponses.add(new MapMoveResponse("⬆️", "North", Hex.Direction.UP));
        menuResponses.add(new MapMoveResponse("↗️", "Northeast", Hex.Direction.UPPER_RIGHT));
        menuResponses.add(new MapMoveResponse("↘️", "Southeast", Hex.Direction.LOWER_RIGHT));
        menuResponses.add(new MapMoveResponse("⬇️", "South", Hex.Direction.DOWN));
        menuResponses.add(new MapMoveResponse("↙️", "Southwest", Hex.Direction.LOWER_LEFT));
        menuResponses.add(new MapMoveResponse("↖️", "Northwest", Hex.Direction.UPPER_LEFT));

        menuResponses.add(new MenuResponse("✅", "Exit") {
            @Override
            public void reactEvent() {
                close(true);
            }

            @Override
            public boolean validConditions() {
                return true;
            }
        });

        menuResponses.add(new MenuResponse("\uD83D\uDCCC", "Display") {
            @Override
            public void reactEvent() {
                close(false);
            }

            @Override
            public boolean validConditions() {
                return true;
            }
        });

    }

    private void move() {
        move(lastDirection);
    }

    private void move(Hex.Direction direction) {
        lastDirection = direction;
        if (cameraBehavior == CameraBehavior.SNAP_TO_PLAYER) {
            player.adjustDestination(direction);
        } else {
            cameraLocation = cameraLocation.move(direction);
        }
    }

    public Hex location() {
        if (cameraBehavior == CameraBehavior.SNAP_TO_PLAYER) {
            return player.getLocation();
        } else {
            return cameraLocation;
        }
    }

    @Override
    public EmbedBuilder getMEBuilder() {

        EmbedBuilder builder = new EmbedBuilder();
        if (map.isMainMap()) {
            builder.setTitle("World Map");
        } else {
            builder.setTitle("Map of " + map.getGuild().getName());
        }
        builder.setDescription(player.getName() + " at " + player.getLocation().toString() + "\n" + claimList());
        builder.setImage(MapRenderer.render(map, location()));

        return builder;
    }

    private String claimList() {

        Tile tile = map.getTileLazy(player.getLocation());

        List<String> lines = new ArrayList<>();
        Nation owningNation = tile.getOwningNation();
        if (owningNation != null) {
            for (Nation nation : Nation.getNations()) {
                Double strength = tile.getNationalStrength(nation);
                if (strength == 0.0) continue;
                String effectiveString = bot.getGuildFor(nation).getName() + ": " + strength;
                if (owningNation == nation) {
                    lines.add("**" + effectiveString + "**");
                } else {
                    lines.add(effectiveString);
                }
            }
        }
        return String.join("\n", lines);
    }

    private class MapMoveResponse extends MenuResponse {

        Hex.Direction direction;

        MapMoveResponse(String emoji, String name, Hex.Direction direction) {
            super(emoji, name);
            this.direction = direction;
        }

        @Override
        public void reactEvent() {
            move(direction);
            updateMessage();
        }

        @Override
        public boolean validConditions() {
            return true;
        }
    }

    private enum CameraBehavior {
        SNAP_TO_PLAYER, CUSTOM;
    }
}
