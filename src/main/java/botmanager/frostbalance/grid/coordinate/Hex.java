package botmanager.frostbalance.grid.coordinate;

/**
 *   ____
 *  /    \
 * /+X  +Y\
 * \      /
 *  \ +Z /
 *   ^^^^
 */

import java.util.*;

/**
 * A Hexagonal Coordinate.
 * Coordinates have no special behavior and are not connected to any grid.
 * They only contain the location in a gridspace for use with other functions.
 * While not strictly static, they have no setter methods for coordinates, only methods
 * for returning new HexCoordinates.
 *
 * This is a nonstandard coordinate system. While most grids use X and Y, where Z is simply
 * -X -Y, this grid has a third value called Z.
 * The coordinates map to an expression of how many steps
 * in each direction you have to take to get to your destination. The vector is immediately normalized on
 * creation into three values: one nonnegative, one zero, and one nonpositive value.
 */
public class Hex {

    public static final double X_SCALE = 40.0;
    public static final double Y_SCALE = 40.0;
    public static final double WIDTH_RATIO = Math.sqrt(3)/2.0;

    long x;
    long y;
    long z;

    public Hex() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Hex(long x, long y, long z) {
        this.x = x;
        this.y = y;
        this.z = z;
        normalize();
    }

    /**
     * Creates a new hex coordinate that somewhat matches the cartesian system.
     * @param cartesianX
     * @param cartesianY
     */
    public Hex(long cartesianX, long cartesianY) {
        x = (long) Math.ceil(-cartesianX / 2.0);
        y = (long) Math.ceil(cartesianX / 2.0);
        z = -cartesianY;
        normalize();
    }

    public static double xSize() {
        return Hex.X_SCALE / Hex.WIDTH_RATIO / 2.0;
    }

    public static double ySize() {
        return Hex.Y_SCALE / Hex.WIDTH_RATIO / 2.0;
    }

    /**
     * Mutates the hex into a normal one.
     */
    private void normalize() {
        long median = getMedian();
        if (median != 0) {
            this.x = x - median;
            this.y = y - median;
            this.z = z - median;
        }
    }

    public Hex move(Direction direction, long value) {
        switch (direction) {
            case NORTH:
                return new Hex(x, y, z - value);
            case NORTHWEST:
                return new Hex(x + value, y, z);
            case SOUTHWEST:
                return new Hex(x, y - value, z);
            case SOUTH:
                return new Hex(x, y, z + value);
            case SOUTHEAST:
                return new Hex(x - value, y, z);
            case NORTHEAST:
                return new Hex(x, y + value, z);
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
        return Hex.origin().subtract(this);
    }

    /**
     * Gets the minimum number of steps to reach a different hex, moving in the given directions.
     * @param hex The hex for comparison.
     * @return The shortest distance between these hexes.
     */
    public long minimumDistance(Hex hex) {
        Hex normalDistanceVector = this.subtract(hex);
        return Math.abs(normalDistanceVector.x) + Math.abs(normalDistanceVector.y) + Math.abs(normalDistanceVector.z);
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

    public Collection<Hex> getHexesToDrawAround(double width, double height) {
        Set<Hex> hexes = new HashSet<>();
        for (int j = (int) (- height / Y_SCALE / 2 - 1); j < height / Y_SCALE / 2 + 1; j++) {
            for (int i = -1; i > - width / X_SCALE / 4 - 2; i--) {

                if (!hexes.add(add(new Hex( i, -i-1, j)))) {
                    System.out.printf("Duplicate at i=%d, j=%d in pass 1/4", i, j);
                }
                if (!hexes.add(add(new Hex( i, -i, j)))) {
                    System.out.printf("Duplicate at i=%d, j=%d in pass 2/4", i, j);
                }
            }
            for (int i = 1; i < width / X_SCALE / 4 + 2; i++) {

                if (!hexes.add(add(new Hex(i - 1, -i, j)))) {
                    System.out.printf("Duplicate at i=%d, j=%d in pass 3/4", i, j);
                }
                if (!hexes.add(add(new Hex(i, -i, j)))) {
                    System.out.printf("Duplicate at i=%d, j=%d in pass 4/4", i, j);
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
        return X_SCALE * (y*WIDTH_RATIO - x*WIDTH_RATIO);
    }

    /**
     * Returns the Y value for use with drawing.
     * The value is scaled according to Y_SCALE.
     */
    public double drawY() {
        return Y_SCALE * (z - y/2.0 - x/2.0);
    }

    public long getX() { return x; }

    public long getY() { return y; }

    public long getZ() { return z; }

    /**
     * Returns the value of X on the offset system.
     * @return The value of y minus the value of x
     */
    public long getOffsetX() {
        return getY() - getX();
    }

    /**
     * Returns the value of Y on the offset system.
     * @return The negative value of Z, after canceling out y and x
     */
    public long getOffsetY() {
        return -getZ() + Math.floorDiv(getY() + getX(), 2);
    }

    /**
     * Display these coordinates on the 'approximation' coordinate system.
     */
    public String getOffsetCoordinates() {
        return "(" + getOffsetX() + ", " + getOffsetY() + ")";
    }


    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Hex)) return false;
        Hex other = ((Hex) object);
        return other.x == x
                && other.y == y
                && other.z == z;
    }

    @Override
    public int hashCode() {

        return Objects.hash(getX(), getY(), getZ());
    }

    public String getCoordinates(CoordSys coordSys) {
        switch (coordSys) {
            case APPROXIMATION:
                return getOffsetCoordinates();
            default:
                return toString();
        }
    }

    /**
     * Display the internal coordinates, ie the 'navigator' coordinate system.
     * These coordinates are picked to encourage players to go northeast, northwest and
     * south. The directional coordinate system also makes for easy navigation between two
     * points, especially from spawn.
     * @return A string containing the X, Y and Z coordinates of this hex
     */
    @Override
    public String toString() {
        return "(" + getX() + "," + getY() + "," + getZ() + ")";
    }

    public Direction crawlDirection() {
        if (getX() > 0) return Direction.NORTHWEST;
        else if (getX() < 0) return Direction.SOUTHEAST;
        else if (getY() > 0) return Direction.NORTHEAST;
        else if (getY() < 0) return Direction.SOUTHWEST;
        else if (getZ() > 0) return Direction.SOUTH;
        else if (getZ() < 0) return Direction.NORTH;
        else throw new IllegalStateException("Origin hex asked to crawl!");
    }

    public List<Direction> crawlDirections() {
        List<Direction> directions = new ArrayList<>();
        Hex instructionHex = new Hex(this.x, this.y, this.z);
        while (!instructionHex.equals(origin())) {
            directions.add(instructionHex.crawlDirection());
            instructionHex = instructionHex.move(instructionHex.crawlDirection(), -1);
            System.out.println(instructionHex);
        }
        return directions;
    }

    public static Hex origin() {
        return new Hex();
    }

    public long getXnoZ() {
        return x - z;
    }

    public long getYnoZ() {
        return y - z;
	}
	
    public enum CoordSys {
        NAVIGATOR, APPROXIMATION;
    }

    public enum Coordinate {
        X, Y, Z;
    }

    /**
     * All directions, starting from up and going clockwise.
     */
    public enum Direction {
        NORTH(1), NORTHWEST(0), SOUTHWEST(5), SOUTH(4), SOUTHEAST(3), NORTHEAST(2);

        final int anglePart;

        Direction(int anglePart) {
            this.anglePart = anglePart;
        }

        public double angle() {
            return (anglePart - 2.5) * 2 * Math.PI / 6.0;
        }

        /**
         * Gets a point on the edge opposite of this one.
         * @param secondPoint
         * @return An edge on a point opposite of this one
         */
        public double xEdge(boolean secondPoint) {
            double anglePortion = anglePart;
            if (secondPoint) anglePortion += 1;
            return Math.cos(anglePortion * 2 * Math.PI / 6.0);
        }

        public double yEdge(boolean secondPoint) {
            double anglePortion = anglePart;
            if (secondPoint) anglePortion += 1;
            return Math.sin(anglePortion * 2 * Math.PI / 6.0);
        }
    }

}
