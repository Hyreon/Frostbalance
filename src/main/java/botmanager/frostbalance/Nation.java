package botmanager.frostbalance;

import java.awt.*;

/**
 * A simple flag to determine which nation something is a part of.
 * Main guilds have the three nations; all other guilds have the NONE nation.
 * The NONE nation can also be used in some contexts for the main nations.
 */
public enum Nation {

    RED(Color.RED), GREEN(Color.GREEN), BLUE(Color.BLUE), NONE(Color.LIGHT_GRAY);

    Color color;

    Nation(Color color) {
        this.color = color;
    }

    /**
     * Returns all nations without NONE
     * @return All nations without NONE
     */
    public static Nation[] getNations() {
        return new Nation[] {RED, GREEN, BLUE};
    }

    public Color getColor() {
        return color;
    }

    public Color adjust(Color color, int drawValue) {
        switch (this) {
            case RED:
                return new Color(drawValue, color.getGreen(), color.getBlue());
            case GREEN:
                return new Color(color.getRed(), drawValue, color.getBlue());
            case BLUE:
                return new Color(color.getRed(), color.getGreen(), drawValue);
            default:
                return color;
        }
    }
}
