package botmanager.frostbalance.grid;

/**
 *   ____
 *  /    \
 * /+X  +Y\
 * \      /
 *  \ +Z /
 *   ^^^^
 */

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A Hexagonal Coordinate.
 * Coordinates have no special behavior and are not connected to any grid.
 * They only contain the location in a gridspace for use with other functions.
 * While not strictly static, they have no setter methods for coordinates, only methods
 * for returning new HexCoordinates.
 *
 * This is a nonstandard coordinate system. While most grids use X and Y, where Z is simply
 * -X -Y, this grid has a third value called Z. Position (X, Y, Z) is the same as (X+1, Y+1, Z+1).
 * The coordinates map to an expression of how many steps
 * in each direction you have to take to get to your destination; since there are multiple ways to reach
 * most coordinates, this is reflected in the coordinate system. Most methods, however, ignore this behavior,
 * preferring to normalize the vector in terms of a nonnegative, a zero, and a nonpositive value.
 */
public class Hex {

    public static final double X_SCALE = 32.0;
    public static final double Y_SCALE = 32.0;
    public static final double WIDTH_RATIO = Math.sqrt(3)/2.0;

    long x;
    long y;
    long z;

    /**
     * An internal cache value to speed up performance.
     * Normal hexes won't have the expensive normalize() function applied to them.
     */
    private boolean isNormal = false;

    public Hex() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        isNormal = true;
    }

    public Hex(long x, long y, long z) {
        this.x = x;
        this.y = y;
        this.z = z;
        isNormal = getMedian() == 0;
    }

    public Hex move(Direction direction, long value) {
        switch (direction) {
            case UP:
                return new Hex(x, y, z - value);
            case UPPER_LEFT:
                return new Hex(x, y + value, z);
            case LOWER_LEFT:
                return new Hex(x - value, y, z);
            case DOWN:
                return new Hex(x, y, z + value);
            case LOWER_RIGHT:
                return new Hex(x, y - value, z);
            case UPPER_RIGHT:
                return new Hex(x + value, y, z);
            default:
                return this;
        }
    }

    public Hex move(Direction direction) {
        return move(direction, 1);
    }

    /**
     * Add all of the values of this hex by another.
     * This is equivalent to translating this hex by the other hex.
     * @param hex The directions for the translation.
     * @return This hex, translated as specified by the other hex.
     */
    public Hex add(Hex hex) {
        return new Hex(x + hex.x, y + hex.y, z + hex.z);
    }

    /**
     * Subtract all of the values of this hex by another.
     * This is equivalent to setting the other hex as this hex's origin and getting the result.
     * @param hex The directions for the translation.
     * @return This hex, translated as specified by the other hex.
     */
    public Hex subtract(Hex hex) {
        return new Hex(x - hex.x, y - hex.y, z - hex.z);
    }

    public Hex negate() {
        return new Hex().subtract(this);
    }

    /**
     * Gets the minimum number of steps to reach a different hex, moving in the given directions.
     * @param hex The hex for comparison.
     * @return The shortest distance between these hexes.
     */
    public long minimumDistance(Hex hex) {
        Hex normalDistanceVector = this.subtract(hex).normalize();
        return Math.abs(normalDistanceVector.x) + Math.abs(normalDistanceVector.y) + Math.abs(normalDistanceVector.z);
    }

    /**
     * Gets the highest value coordinate. Note that this is not normalized.
     * @return The highest value coordinate.
     */
    public long getMaximum() {
        return Math.max(Math.max(x, y), z);
    }

    /**
     * Gets the median value coordinate. Note that this is not normalized.
     * @return The median value coordinate.
     */
    public long getMedian() {
        if ((z >= x && x >= y) || (y >= x && x >= z)) {
            return x;
        } else if ((x >= y && y >= z) || (z >= y && y >= x)) {
            return y;
        } else {
            return z;
        }
    }

    /**
     * Gets the lowest value coordinate. Note that this is not normalized.
     * @return The lowest value coordinate.
     */
    public long getMinimum() {
        return Math.min(Math.min(x, y), z);
    }

    /**
     * Gets this Hex, but with the median value at 0. This means one value is positive and one is negative.
     * Note that because most functions will treat a non-normalized Hex the same as a normalized Hex,
     * this function isn't necessary for most things.
     * @return
     */
    public Hex normalize() {
        if (isNormal) return this;
        return new Hex(x - getMedian(), y - getMedian(), z - getMedian());
    }

    public Collection<Hex> getHexesToDrawAround(int width, int height) {
        Set<Hex> hexes = new HashSet<>();
        for (int j = (int) (- height / Y_SCALE / 2 - 1); j < height / Y_SCALE / 2 + 1; j++) {
            for (int i = -1; i > - width / X_SCALE / 4 - 2; i--) {

                if (!hexes.add(add(new Hex( i, -i-1, j)))) {
                    System.out.printf("Duplicate at i=%i, j=%i in pass 1/4", i, j);
                }
                if (!hexes.add(add(new Hex( i, -i, j)))) {
                    System.out.printf("Duplicate at i=%i, j=%i in pass 2/4", i, j);
                }
            }
            for (int i = 1; i < width / X_SCALE / 4 + 2; i++) {

                if (!hexes.add(add(new Hex(i - 1, -i, j)))) {
                    System.out.printf("Duplicate at i=%i, j=%i in pass 3/4", i, j);
                }
                if (!hexes.add(add(new Hex(i, -i, j)))) {
                    System.out.printf("Duplicate at i=%i, j=%i in pass 4/4", i, j);
                }
            }
            if (!hexes.add(add(new Hex(0, 0, j)))) {
                System.out.println("Duplicate at j=" + j);
            }
        }
        return hexes;
    }

    /**
     * Returns the X value for use with drawing.
     * The value is scaled according to X_SCALE.
     */
    public double drawX() {
        return X_SCALE * (x*WIDTH_RATIO - y*WIDTH_RATIO);
    }

    /**
     * Returns the Y value for use with drawing.
     * The value is scaled according to Y_SCALE.
     */
    public double drawY() {
        return Y_SCALE * (z - y/2.0 - x/2.0);
    }

    public long getX() { return normalize().x; }

    public long getY() { return normalize().y; }

    public long getZ() { return normalize().z; }

    public long getInternalX() {
        return x;
    }

    public long getInternalY() {
        return y;
    }

    public long getInternalZ() {
        return z;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Hex)) return false;
        Hex other = ((Hex) object).normalize();
        Hex selfForComparison = this.normalize();
        return other.x == selfForComparison.x
                && other.y == selfForComparison.y
                && other.z == selfForComparison.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY(), getZ());
    }


    @Override
    public String toString() {
        return "(" + getX() + "," + getY() + "," + getZ() + ")";
    }

    public enum Coordinate {
        X, Y, Z;
    }

    /**
     * All directions, starting from up and going clockwise.
     */
    public enum Direction {
        UP, UPPER_LEFT, LOWER_LEFT, DOWN, LOWER_RIGHT, UPPER_RIGHT;
    }

}
