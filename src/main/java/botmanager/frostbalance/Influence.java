package botmanager.frostbalance;

/**
 * A simple wrapper around a double.
 * As we were already using the Double class in most cases
 * rather than a primitive type, this should not significantly
 * reduce performance.
 * This module automatically rounds to 0.001 level precision,
 * has a built-in to-string method that displays the double rapidly
 * and correctly, and can be changed to work better internally by
 * avoiding floating-point precision problems.
 * A lot of the methods here are very low-level, ie they
 * use very fast implementations rather than being focused on readability.
 */
public class Influence {

    int thousandths;

    public Influence(int thousandths) {
        this.thousandths = thousandths; //no need to round, it's already guaranteed to be precise enough
    }

    public Influence(double value) {
        this.thousandths = (int) Math.round(value * 1000);
    }

    public Influence(String string) {
        String[] words = string.split(".");
        thousandths = Integer.parseInt(words[0]) * 1000;
        if (words[1].length() > 3) {
            words[1] = words[1].substring(0,3);
        }
        while (words[1].length() < 3) {
            words[1] += "0";
        }
        thousandths += Integer.parseInt(words[1]);
    }

    public double getValue() {
        return thousandths / 1000.0;
    }

    public int getThousandths() {
        return thousandths;
    }

    public int compareTo(Influence influence) {
        return thousandths - influence.thousandths; //very sad that you can't do shift operations on doubles :(
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Influence) && ((Influence) o).thousandths == thousandths;
    }

    @Override
    public int hashCode() {
        return getThousandths();
    }

    @Override
    public String toString() {
        return Double.toString(getValue());
    }

    public Influence add(Double strength) {
        return new Influence(strength + getValue());
    }

    public Influence add(Influence other) {
        return new Influence(other.thousandths + thousandths);
    }

    public Influence subtract(Influence other) {
        return new Influence(thousandths - other.thousandths);
    }

    public Influence negate() {
        return new Influence(-thousandths);
    }

    public Influence applyModifier(double modifier) {
        return new Influence(getValue() * modifier);
    }
}
