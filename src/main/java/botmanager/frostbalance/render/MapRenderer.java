package botmanager.frostbalance.render;

import botmanager.frostbalance.Nation;
import botmanager.frostbalance.grid.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MapRenderer {

    private static final int DEFAULT_HEIGHT = 300;
    private static final int DEFAULT_WIDTH = 400;
    private static final int BCOLOR = 48;

    //TODO
    public static String render(WorldMap map, Hex center) {
        BufferedImage image = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        for (Hex drawHex : center.getHexesToDrawAround(DEFAULT_WIDTH, DEFAULT_HEIGHT)) {
            renderTile(g, map.getTileLazy(drawHex), center);
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

    private static void renderTile(Graphics g, Tile tile, Hex center) {
        System.out.println("Rendering " + tile.getLocation());
        Hex drawnHex = tile.getLocation().subtract(center);
        g.setColor(getPoliticalColor(tile));
        g.fillPolygon(getHex(drawnHex));
        for (TileObject object : tile.getObjects()) {
            try {
                BufferedImage image = object.getImage();
                if (image == null) continue;
                g.drawImage(image, (int) (drawnHex.drawX() - Hex.X_SCALE/2 + DEFAULT_WIDTH/2),
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
        g.setColor(Color.BLACK);
        g.drawPolygon(getHex(drawnHex));
    }

    private static Polygon getHex(Hex hex) {

        double xDist = Hex.X_SCALE/Hex.WIDTH_RATIO/2.0;
        double yDist = Hex.Y_SCALE/Hex.WIDTH_RATIO/2.0;

        Polygon p = new Polygon();
        for (int i = 0; i < 6; i++) {
            p.addPoint((int) (hex.drawX() - xDist * Math.cos(i * 2 * Math.PI / 6) + DEFAULT_WIDTH/2),
                    (int) (hex.drawY() - yDist * Math.sin(i * 2 * Math.PI / 6) + DEFAULT_HEIGHT/2));
        }
        return p;
    }

    private static Color getPoliticalColor(Tile tile) {
        Nation owningNation = tile.getOwningNation();
        Color color = Color.BLACK;
        if (owningNation != null) {
            //darken according to fraction of strongest political color.
            for (Nation nation : Nation.getNations()) {
                double ratio = tile.getNationalStrength(nation)
                        / tile.getMap().getStrongestClaim();
                int drawValue;
                if (nation != owningNation) {
                    drawValue = (BCOLOR + (int) ((255 - BCOLOR) * ratio)) / 2;
                } else {
                    drawValue = BCOLOR + (int) ((255 - BCOLOR) * ratio);
                }
                System.out.println("Ratio: " + ratio);
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
