package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.action.actions.DummyAction;
import botmanager.frostbalance.command.GuildMessageContext;
import botmanager.frostbalance.command.MessageContext;
import botmanager.frostbalance.grid.ClaimData;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.WorldMap;
import botmanager.frostbalance.grid.coordinate.Hex;
import botmanager.frostbalance.menu.response.MenuResponse;
import botmanager.frostbalance.menu.response.SimpleTextHook;
import botmanager.frostbalance.render.MapRenderer;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class MapMenu extends Menu {

    private WorldMap map;

    private PlayerCharacter player;

    /**
     * The location of the camera when not snapping to the players' location.
     */
    private Hex cameraLocation;

    private CameraBehavior cameraBehavior = CameraBehavior.SNAP_TO_PLAYER;
    private double zoomFactor;

    public MapMenu(Frostbalance bot, GuildMessageContext context) {
        super(bot, context);
        this.map = context.getGameNetwork().getWorldMap();
        this.player = context.getPlayer().getCharacter();
        this.zoomFactor = context.getAuthor().getUserOptions().getZoomSize();

        menuResponses.add(new MenuResponse("\uD83D\uDCCD", "Create waypoint") {

            @Override
            public void reactEvent() {
                player.getActionQueue().add(new DummyAction(player));
                updateMessage();
            }

            @Override
            public boolean isValid() {
                return true;
            }

        });

        menuResponses.add(new MenuResponse("⏪", "Undo last action") {

            @Override
            public void reactEvent() {
                player.getActionQueue().removeLast();
                updateMessage();
            }

            @Override
            public boolean isValid() {
                return !player.getActionQueue().isEmpty();
            }
        });

        menuResponses.add(new MenuResponse("⏫", "Zoom out") {

            @Override
            public void reactEvent() {
                zoomFactor /= 1.75;
                updateMessage();
            }

            @Override
            public boolean isValid() {
                return zoomFactor > 1.0 / 8;
            }

        });

        menuResponses.add(new MenuResponse("⏬", "Zoom in") {

            @Override
            public void reactEvent() {
                zoomFactor *= 1.75;
                updateMessage();
            }

            @Override
            public boolean isValid() {
                return zoomFactor < 4;
            }

        });

        menuResponses.add(new MenuResponse("\uD83D\uDD04", "Refresh map") {

            @Override
            public void reactEvent() {
                updateMessage();
            }

            @Override
            public boolean isValid() {
                return true;
            }

        });

        menuResponses.add(new MenuResponse("✅", "Exit") {
            @Override
            public void reactEvent() {
                close(true);
            }

            @Override
            public boolean isValid() {
                return true;
            }
        });

        menuResponses.add(new MenuResponse("\uD83D\uDCCC", "Display") {
            @Override
            public void reactEvent() {
                close(false);
            }

            @Override
            public boolean isValid() {
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
            public boolean isValid() {
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
            public boolean isValid() {
                return getCameraBehavior() != CameraBehavior.SNAP_TO_PLAYER;
            }
        });

        hook(new SimpleTextHook(this, "Or type <DIRECTION> <AMOUNT> to move; eg NORTH 1") {

            @Override
            public void hookEvent(@NotNull MessageContext hookContext) {
                String[] args = hookContext.getMessage().getContentRaw().split(" ");
                Hex.Direction direction = Hex.Direction.valueOf(Hex.Direction.class, args[0].toUpperCase(Locale.ROOT));
                int amount = Integer.parseInt(args[1]);
                move(direction, amount);
                updateMessage();
            }

            @Override
            public boolean isValid(@NotNull MessageContext hookContext) {
                String[] directionNames = new String[Hex.Direction.values().length];
                for (int i = 0; i < Hex.Direction.values().length; i++) {
                    directionNames[i] = "(" + Hex.Direction.values()[i].name().toUpperCase() + ")";
                }
                String pattern = "(" + String.join("|", directionNames) + ") [0-9]{1,3}";
                System.out.println(pattern);
                String content = hookContext.getMessage().getContentRaw().toUpperCase();
                System.out.println(content);
                return content.matches(pattern);
            }
        });

    }

    public MapMenu(Frostbalance bot, GuildMessageContext context, Hex destination) {
        this(bot, context);
        cameraLocation = destination;
        cameraBehavior = CameraBehavior.CUSTOM;
    }

    private CameraBehavior getCameraBehavior() {
        return cameraBehavior;
    }

    private void move(Hex.Direction direction, int amount) {
        if (cameraBehavior == CameraBehavior.SNAP_TO_PLAYER) {
            player.adjustDestination(direction, amount);
        } else {
            cameraLocation = cameraLocation.move(direction, amount);
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
    public EmbedBuilder getEmbedBuilder() {

        EmbedBuilder builder = new EmbedBuilder();
        if (map.getGameNetwork().isMain()) {
            builder.setTitle("World Map");
        } else {
            builder.setTitle("Map of " + map.getGameNetwork().getId());
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
        builder.setImage(MapRenderer.render(map, drawLocation(), zoomFactor));

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
            move(direction, 1);
            updateMessage();
        }

        @Override
        public boolean isValid() {
            return true;
        }
    }

    private enum CameraBehavior {
        SNAP_TO_PLAYER, CUSTOM;
    }
}
