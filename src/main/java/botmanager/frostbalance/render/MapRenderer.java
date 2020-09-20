package botmanager.frostbalance.render;

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
import java.util.Iterator;

//TODO make the map renderer more sane.
public class MapRenderer {

    private static final int DEFAULT_HEIGHT = 300;
    private static final int DEFAULT_WIDTH = 400;

    public static String render(WorldMap map, Hex center, double size_factor) {
        BufferedImage image = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        //RenderingHints rh = new RenderingHints(
        //        RenderingHints.KEY_TEXT_ANTIALIASING,
        //        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        //g.setRenderingHints(rh);
        Collection<Hex> drawHexes = center.getHexesToDrawAround(DEFAULT_WIDTH / size_factor, DEFAULT_HEIGHT / size_factor);
        for (Hex drawHex : drawHexes) {
            renderTile(g, map.getRenderTile(drawHex), center, size_factor);
        }
        for (Hex drawHex : drawHexes) {
            drawBorders(g, map.getRenderTile(drawHex), center, size_factor);
        }
        for (Hex drawHex : drawHexes) {
            renderTileObjects(g, map.getRenderTile(drawHex), center, size_factor);
        }
        g.dispose();

        try {
            ImageIO.write(image, "png", new File("maprender.png"));
            String link;
            try {
                String resultJson = net.dv8tion.imup.Uploader.upload(new File("maprender.png"));
                System.out.println(resultJson);
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

    private static void renderTileObjects(Graphics2D g, Tile tile, Hex center, double size_factor) {
        Hex drawnHex = tile.getLocation().subtract(center);
        for (TileObject object : tile.getObjects()) {
            try {
                BufferedImage image = object.getImage();
                if (image == null) continue;
                if (object instanceof PlayerCharacter) {
                    PlayerCharacter player = (PlayerCharacter) object;
                    if (player.getDestination() != player.getLocation()) {
                        renderMovementLine(g, player.getLocation().subtract(center), player.getDestination().subtract(center), player, size_factor);
                    }
                }
                int width = image.getWidth();
                BufferedImage circleBuffer = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = circleBuffer.createGraphics();
                g2.setClip(new Ellipse2D.Float(0, 0, width, width));
                g2.drawImage(image, 0, 0, width, width, null);
                g.drawImage(circleBuffer, (int) ((drawnHex.drawX() - Hex.X_SCALE/2)*size_factor + DEFAULT_WIDTH/2),
                        (int) ((drawnHex.drawY() - Hex.Y_SCALE/2)*size_factor + DEFAULT_HEIGHT/2),
                        (int) (Hex.X_SCALE * size_factor),
                        (int) (Hex.Y_SCALE * size_factor),
                        null);
                System.out.println("Draw object at " + tile.getLocation());
            } catch (IOException e) {
                System.err.println("IOException when trying to render a tile object");
                e.printStackTrace();
            }
        }
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
     * @param destination
     * @param size_factor
     */
    private static void renderMovementLine(Graphics2D g, Hex location, Hex destination, PlayerCharacter playerCharacter, double size_factor) {
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        Color color;
        if (playerCharacter.getNation() != null) {
            color = playerCharacter.getNation().getColor();
        } else color = Color.GRAY;
        g.setColor(new Color(Math.max(128, color.getRed()), Math.max(128, color.getGreen()), Math.max(128, color.getBlue())));
        Hex offset = Hex.origin();
        System.out.println("Movement line: " + destination.subtract(location));
        Iterator<Hex.Direction> instructions = destination.subtract(location).crawlDirections();
        while (instructions.hasNext()) {
            Hex.Direction nextDirection = instructions.next();
            g.drawLine((int)(location.add(offset).drawX() * size_factor) + DEFAULT_WIDTH/2,
                    (int)(location.add(offset).drawY() * size_factor) + DEFAULT_HEIGHT/2,
                    (int)(location.add(offset.move(nextDirection)).drawX() * size_factor) + DEFAULT_WIDTH/2,
                    (int)(location.add(offset.move(nextDirection)).drawY() * size_factor) + DEFAULT_HEIGHT/2);
            offset = offset.move(nextDirection);
        }
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
