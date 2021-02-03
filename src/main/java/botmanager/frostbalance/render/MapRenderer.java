package botmanager.frostbalance.render;

import botmanager.frostbalance.Player;
import botmanager.frostbalance.action.ActionQueue;
import botmanager.frostbalance.grid.*;
import botmanager.frostbalance.grid.building.Gatherer;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.Tile;
import botmanager.frostbalance.grid.TileObject;
import botmanager.frostbalance.grid.WorldMap;
import botmanager.frostbalance.grid.coordinate.Hex;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.imup.WebException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

//TODO make the map renderer more sane.
public class MapRenderer {

    private static final int DEFAULT_HEIGHT = 300;
    private static final int DEFAULT_WIDTH = 400;

    public static String render(Player player, WorldMap map, Hex center, double sizeFactor) {
        BufferedImage image = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        //RenderingHints rh = new RenderingHints(
        //        RenderingHints.KEY_TEXT_ANTIALIASING,
        //        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //g.setRenderingHints(rh);
        Collection<Hex> drawHexes = center.getHexesToDrawAround(DEFAULT_WIDTH / sizeFactor, DEFAULT_HEIGHT / sizeFactor);
        for (Hex drawHex : drawHexes) {
            renderTile(g, map.getRenderTile(drawHex), center, sizeFactor);
        }
        //for (Hex drawHex : drawHexes) {
        //    drawBorders(g, map.getRenderTile(drawHex), center, size_factor);
        //}
        if (player.getUserWrapper().getUserOptions().getDrawCoords()) {
            for (Hex drawHex : drawHexes) {
                renderTileCoordinates(g, player, map.getRenderTile(drawHex), center, sizeFactor);
            }
        }
        for (Hex drawHex : drawHexes) {
            Tile tile = map.getRenderTile(drawHex);
            for (Mobile mob : tile.getMobs()) {
                renderObject(g, tile, center, sizeFactor, mob);
            }
            Gatherer activeGatherer = tile.getBuildingData().activeGatherer();
            if (activeGatherer != null) {
                renderObject(g, tile, center, sizeFactor, activeGatherer);
            }
		}
        g.dispose();

        try {
            ImageIO.write(image, "png", new File("maprender.png"));
            String link;
            try {
                String resultJson = net.dv8tion.imup.Uploader.upload(new File("maprender.png"));
                JsonObject obj = JsonParser.parseString(resultJson).getAsJsonObject();
                link = obj.get("data").getAsJsonObject().get("link").getAsString();
            } catch (WebException e) {
                e.printStackTrace();
                System.out.println("Found an exception uploading to imgur. Attempting manual file upload");
                link = "attachment://maprender.png";
            }
            return link;
        } catch (IOException e) {
            System.err.println("Unable to write map file!");
            return null;
        }

    }

    private static void renderObject(Graphics2D g, Tile tile, Hex center, double size_factor, Renderable object) {
        Hex drawnHex = tile.getLocation().subtract(center);
        try {
            BufferedImage image = object.getImage();
            if (image == null) return;
            if (object instanceof PlayerCharacter) {
                PlayerCharacter player = (PlayerCharacter) object;
                if (player.getDestination() != player.getLocation()) {
                    renderMovementLine(g, player.getLocation().subtract(center), player, size_factor);
                }
            }
            int width = image.getWidth();
            BufferedImage circleBuffer = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = circleBuffer.createGraphics();
            g2.setClip(new Ellipse2D.Float(0, 0, width, width));
            g2.drawImage(image, 0, 0, width, width, null);
            g.drawImage(circleBuffer, (int) ((drawnHex.drawX() - Hex.X_SCALE / 2) * size_factor + DEFAULT_WIDTH / 2),
                    (int) ((drawnHex.drawY() - Hex.Y_SCALE / 2) * size_factor + DEFAULT_HEIGHT / 2),
                    (int) (Hex.X_SCALE * size_factor),
                    (int) (Hex.Y_SCALE * size_factor),
                    null);
            System.out.println("Draw object at " + tile.getLocation());
        } catch (IOException e) {
            System.err.println("IOException when trying to render a tile object");
            e.printStackTrace();
        }
    }
	
    private static void renderTileCoordinates(Graphics2D g, Player renderer, Tile tile, Hex center, double sizeFactor) {
        int capacity = (int) (1.0 / (sizeFactor));

        Hex drawnHex = tile.getLocation().subtract(center);

        if (capacity > 0 && (
                drawnHex.getX() % capacity != 0 ||
                drawnHex.getY() % capacity != 0 ||
                drawnHex.getZ() % capacity != 0)) return;

        g.setColor(Color.BLACK);
        g.drawString(tile.getLocation().getCoordinates(renderer.getUserWrapper().getUserOptions().getCoordSys()),
                (int) ((drawnHex.drawX())*sizeFactor + DEFAULT_WIDTH/2),
                (int) ((drawnHex.drawY())*sizeFactor + DEFAULT_HEIGHT/2));
    }

    private static void renderTile(Graphics2D g, Tile tile, Hex center, double size_factor) {
        g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        Hex drawnHex = tile.getLocation().subtract(center);
        g.setColor(tile.getBiomeColor());
        g.fillPolygon(getHex(drawnHex, size_factor));
    }

    private static void drawBorders(Graphics2D g, Tile tile, Hex center, double size_factor) {
        g.setColor(tile.getPoliticalBorderColor());
        g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

        for (Hex.Direction direction : Hex.Direction.values()) {
            Tile neighbor = tile.getNeighbor(direction);
            Hex hex = tile.getLocation().subtract(center);

            int x1 = (int) ((hex.drawX() - Hex.xSize() * direction.xEdge(false))*size_factor + DEFAULT_WIDTH / 2);
            int y1 = (int) ((hex.drawY() - Hex.ySize() * direction.yEdge(false))*size_factor + DEFAULT_HEIGHT / 2);
            int x2 = (int) ((hex.drawX() - Hex.xSize() * direction.xEdge(true))*size_factor + DEFAULT_WIDTH / 2);
            int y2 = (int) ((hex.drawY() - Hex.ySize() * direction.yEdge(true))*size_factor + DEFAULT_HEIGHT / 2);

            if ((neighbor.getClaimData().getOwningNation() != tile.getClaimData().getOwningNation())
                    && (tile.getClaimData().getOwningNation() != null)
                    && (neighbor.getClaimData().getOwningNation() != null)) {
                //draw two lines of different colors, using a midpoint
                int x3 = (x1 + x2) / 2;
                int y3 = (y1 + y2) / 2;
                g.drawLine(x1, y1, x3, y3);
                g.setColor(neighbor.getPoliticalBorderColor());
                g.drawLine(x3, y3, x2, y2);
                g.setColor(tile.getPoliticalBorderColor());
            } else if (neighbor.getClaimData().getOwningNation() == tile.getClaimData().getOwningNation()
                    || (tile.getClaimData().getOwningNation() != null)) {
                //draw line normally
                g.drawLine(x1, y1, x2, y2);
            }
        }

    }

    /**
     * The values put in here must already be offset, using the center as the origin.
     * @param g
     * @param location
     * @param size_factor
     */
    private static void renderMovementLine(Graphics2D g, Hex location, PlayerCharacter playerCharacter, double size_factor) {
        g.setStroke(new BasicStroke(0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        Color color;
        if (playerCharacter.getNation() != null) {
            color = playerCharacter.getNation().getColor();
        } else color = Color.GRAY;
        g.setColor(new Color(Math.max(128, color.getRed()), Math.max(128, color.getGreen()), Math.max(128, color.getBlue())));
        List<Hex> waypoints = playerCharacter.getActionQueue().simulation().waypoints(false);
        for (Hex waypoint : waypoints) {
            g.drawOval((int)(location.add(waypoint.subtract(playerCharacter.getLocation())).drawX() * size_factor) + DEFAULT_WIDTH/2 - 4,
                    (int)(location.add(waypoint.subtract(playerCharacter.getLocation())).drawY() * size_factor) + DEFAULT_HEIGHT/2 - 4,
                    8,
                    8);
        }
        Hex offset = Hex.origin();
        List<Hex.Direction> instructions = playerCharacter.getActionQueue().simulation().moves();
        for (Hex.Direction nextDirection : instructions) {
            //g.drawLine((int)(location.add(offset).drawX() * size_factor) + DEFAULT_WIDTH/2,
            //        (int)(location.add(offset).drawY() * size_factor) + DEFAULT_HEIGHT/2,
            //        (int)(location.add(offset.move(nextDirection)).drawX() * size_factor) + DEFAULT_WIDTH/2,
            //        (int)(location.add(offset.move(nextDirection)).drawY() * size_factor) + DEFAULT_HEIGHT/2);
            g.fillPolygon(getMovementArrow(location.add(offset), nextDirection, size_factor));
            offset = offset.move(nextDirection);
        }
    }

    private static Polygon getMovementArrow(Hex location, Hex.Direction direction, double size_factor) {

        final double TRIANGLE_SIZE = 4 * Math.sqrt(2);

        Polygon triangle = new Polygon();
        double angle = direction.angle();
        int baseX = (int)((location.drawX() + Hex.X_SCALE/2 * Math.cos(angle)) * size_factor);
        int baseY = (int)((location.drawY() + Hex.Y_SCALE/2 * Math.sin(angle)) * size_factor);
        triangle.addPoint((int) (baseX + TRIANGLE_SIZE * Math.cos(angle) + DEFAULT_WIDTH/2),
                (int) (baseY + TRIANGLE_SIZE * Math.sin(angle) + DEFAULT_HEIGHT/2));
        triangle.addPoint(
                (int) (baseX + TRIANGLE_SIZE * Math.cos(angle + Math.PI/2) + DEFAULT_WIDTH/2),
                (int) (baseY + TRIANGLE_SIZE * Math.sin(angle + Math.PI/2) + DEFAULT_HEIGHT/2));
        triangle.addPoint(
                (int) (baseX + TRIANGLE_SIZE * Math.cos(angle - Math.PI/2) + DEFAULT_WIDTH/2),
                (int) (baseY + TRIANGLE_SIZE * Math.sin(angle - Math.PI/2) + DEFAULT_HEIGHT/2));
        return triangle;

    }

    private static Polygon getHex(Hex hex, double size_factor) {

        Polygon p = new Polygon();
        for (int i = 0; i < 6; i++) {
            p.addPoint((int) ((hex.drawX() - Hex.xSize() * Math.cos(i * 2 * Math.PI / 6))*size_factor + DEFAULT_WIDTH / 2),
                    (int) ((hex.drawY() - Hex.ySize() * Math.sin(i * 2 * Math.PI / 6))*size_factor + DEFAULT_HEIGHT / 2));
        }
        return p;
    }

    public enum Mode {

        /**
         * Emphasizes the claims that players have made on the land. Renders faction colors.
         */
        POLITICAL,

        /**
         * Emphasizes the status of natural resources in the area.
         */
        ECONOMIC,

        /**
         * Emphasizes your relation to the owner of every claim.
         */
        SOCIAL,

        /**
         * Shows tactical information, player locations and blind spots.
         */
        MILITARY,

        /**
         * Apply no color filter to the terrain based on claims.
         */
        BASE;
    }
}
