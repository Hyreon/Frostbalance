package botmanager.frostbalance.render;

import botmanager.frostbalance.Nation;
import botmanager.frostbalance.grid.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

//TODO make the map renderer more sane.
public class MapRenderer {

    private static final int DEFAULT_HEIGHT = 300;
    private static final int DEFAULT_WIDTH = 400;
    private static final int BCOLOR = 48;

    public static String render(WorldMap map, Hex center) {
        BufferedImage image = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        for (Hex drawHex : center.getHexesToDrawAround(DEFAULT_WIDTH, DEFAULT_HEIGHT)) {
            renderTile(g, map.getTileLazy(drawHex), center);
        }
        for (Hex drawHex : center.getHexesToDrawAround(DEFAULT_WIDTH, DEFAULT_HEIGHT)) {
            renderTileObjects(g, map.getTileLazy(drawHex), center);
        }
        g.dispose();

        try {
            ImageIO.write(image, "png", new File("maprender.png"));
            return "attachment://maprender.png";
        } catch (IOException e) {
            System.err.println("Unable to write map file!");
            return null;
        }

    }

    private static void renderTileObjects(Graphics2D g, Tile tile, Hex center) {
        Hex drawnHex = tile.getLocation().subtract(center);
        for (TileObject object : tile.getObjects()) {
            try {
                BufferedImage image = object.getImage();
                if (image == null) continue;
                if (object instanceof PlayerCharacter) {
                    PlayerCharacter player = (PlayerCharacter) object;
                    if (player.getDestination() != player.getLocation()) {
                        renderMovementLine(g, player.getLocation().subtract(center), player.getDestination().subtract(center), player);
                    }
                }
                int width = image.getWidth();
                BufferedImage circleBuffer = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = circleBuffer.createGraphics();
                g2.setClip(new Ellipse2D.Float(0, 0, width, width));
                g2.drawImage(image, 0, 0, width, width, null);
                g.drawImage(circleBuffer, (int) (drawnHex.drawX() - Hex.X_SCALE/2 + DEFAULT_WIDTH/2),
                        (int) (drawnHex.drawY() - Hex.Y_SCALE/2 + DEFAULT_HEIGHT/2),
                        (int) Hex.X_SCALE,
                        (int) Hex.Y_SCALE,
                        null);
                System.out.println("Draw object at " + tile.getLocation());
            } catch (IOException e) {
                System.err.println("IOException when trying to render a tile object");
                e.printStackTrace();
            }
        }
    }

    private static void renderTile(Graphics2D g, Tile tile, Hex center) {
        g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        Hex drawnHex = tile.getLocation().subtract(center);
        g.setColor(getPoliticalColor(tile));
        g.fillPolygon(getHex(drawnHex));
        g.setColor(Color.BLACK);
        g.drawPolygon(getHex(drawnHex));
    }

    /**
     * The values put in here must already be offset, using the center as the origin.
     * @param g
     * @param location
     * @param destination
     */
    private static void renderMovementLine(Graphics2D g, Hex location, Hex destination, PlayerCharacter playerCharacter) {
        g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        Color color = playerCharacter.getNation().getColor();
        g.setColor(new Color(Math.max(128, color.getRed()), Math.max(128, color.getGreen()), Math.max(128, color.getBlue())));
        Hex offset = Hex.origin();
        System.out.println("Movement line: " + destination.subtract(location));
        Iterator<Hex.Direction> instructions = destination.subtract(location).crawlDirections();
        while (instructions.hasNext()) {
            Hex.Direction nextDirection = instructions.next();
            g.drawLine((int)location.add(offset).drawX() + DEFAULT_WIDTH/2,
                    (int)location.add(offset).drawY() + DEFAULT_HEIGHT/2,
                    (int)location.add(offset.move(nextDirection)).drawX() + DEFAULT_WIDTH/2,
                    (int)location.add(offset.move(nextDirection)).drawY() + DEFAULT_HEIGHT/2);
            offset = offset.move(nextDirection);
        }
    }

    private static Polygon getHex(Hex hex) {

        double xDist = Hex.X_SCALE / Hex.WIDTH_RATIO / 2.0;
        double yDist = Hex.Y_SCALE / Hex.WIDTH_RATIO / 2.0;

        Polygon p = new Polygon();
        for (int i = 0; i < 6; i++) {
            p.addPoint((int) (hex.drawX() - xDist * Math.cos(i * 2 * Math.PI / 6) + DEFAULT_WIDTH / 2),
                    (int) (hex.drawY() - yDist * Math.sin(i * 2 * Math.PI / 6) + DEFAULT_HEIGHT / 2));
        }
        return p;
    }

    private static Color getPoliticalColor(Tile tile) {
        Nation owningNation = tile.getClaimData().getOwningNation();
        Color color = Color.BLACK;
        if (owningNation != Nation.NONE) {
            //darken according to fraction of strongest political color.
            for (Nation nation : Nation.getNations()) {
                double ratio = tile.getClaimData().getNationalStrength(nation)
                        / tile.getMap().getStrongestClaim();
                int drawValue;
                if (nation != owningNation) {
                    drawValue = (BCOLOR + (int) ((255 - BCOLOR) * ratio)) / 2;
                } else {
                    drawValue = BCOLOR + (int) ((255 - BCOLOR) * ratio);
                }
                System.out.println("DrawValue: " + drawValue);
                color = nation.adjustDisplayColor(color, drawValue);
            }
        } else {
            color = new Color(BCOLOR, BCOLOR, BCOLOR);
        }
        return color;
    }

    public static String render(PlayerCharacter playerCharacter) {
        return render(playerCharacter.getMap(), playerCharacter.getLocation());
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
         * Apply no color filter to the terrain based on claims.
         */
        BASE;
    }
}
