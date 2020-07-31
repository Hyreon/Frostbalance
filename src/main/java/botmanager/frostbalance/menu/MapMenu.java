package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.grid.ClaimData;
import botmanager.frostbalance.grid.Hex;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.WorldMap;
import botmanager.frostbalance.render.MapRenderer;
import net.dv8tion.jda.api.EmbedBuilder;

public class MapMenu extends Menu {

    private WorldMap map;

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

        menuResponses.add(new MenuResponse("\uD83D\uDCF9", "Freecam") {

            @Override
            public void reactEvent() {
                cameraLocation = drawLocation();
                cameraBehavior = CameraBehavior.CUSTOM;
                updateMessage();
            }

            @Override
            public boolean validConditions() {
                return getCameraBehavior() != CameraBehavior.CUSTOM;
            }
        });

        menuResponses.add(new MenuResponse("♟️", "Snap to self") {

            @Override
            public void reactEvent() {
                cameraBehavior = CameraBehavior.SNAP_TO_PLAYER;
                cameraLocation = drawLocation();
                updateMessage();
            }

            @Override
            public boolean validConditions() {
                return getCameraBehavior() != CameraBehavior.SNAP_TO_PLAYER;
            }
        });

    }

    public MapMenu(Frostbalance bot, WorldMap map, PlayerCharacter player, Hex destination) {
        this(bot, map, player);
        cameraLocation = destination;
        cameraBehavior = CameraBehavior.CUSTOM;
    }

    private CameraBehavior getCameraBehavior() {
        return cameraBehavior;
    }

    private void move(Hex.Direction direction) {
        if (cameraBehavior == CameraBehavior.SNAP_TO_PLAYER) {
            player.adjustDestination(direction);
        } else {
            cameraLocation = cameraLocation.move(direction);
        }
    }

    public Hex drawLocation() {
        if (cameraBehavior == CameraBehavior.SNAP_TO_PLAYER) {
            return player.getDestination();
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
        String description = "";
        if (cameraBehavior == CameraBehavior.SNAP_TO_PLAYER) {
            description += player.getName() + " at ";
            if (!drawLocation().equals(player.getLocation())) {
                description += player.getLocation() + " ➤ ";
            }
        } else {
            description += player.getName() + "'s view of ";
        }
        description += drawLocation() + "\n" + player.getTile().getMap().getTile(drawLocation()).getClaimData().displayClaims(ClaimData.Format.SIMPLE);
        builder.setDescription(description);
        builder.setImage(MapRenderer.render(map, drawLocation()));

        return builder;
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
