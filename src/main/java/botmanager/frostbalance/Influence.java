package botmanager.frostbalance;

import com.google.gson.annotations.JsonAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A simple wrapper around a double.
 * As we were already using the Double class in most cases
 * rather than a primitive type, this should not significantly
 * reduce performance.
 * This module automatically rounds to 0.001 level precision,
 * has a built-in to-string method that displays the double rapidly
 * and correctly, and can be changed to work better internally by
 * avoiding floating-point precision problems.
 */
@JsonAdapter(value=InfluenceAdapter.class)
public class Influence {

    int thousandths;


    public Influence(int thousandths) {
        this.thousandths = thousandths; //no need to round, it's already guaranteed to be precise enough
    }

    public Influence(double value) {
        this.thousandths = (int) Math.round(value * 1000);
    }

    public Influence(String string) {
        double inputValue = Double.parseDouble(string);
        if (Double.isNaN(inputValue)) {
            throw new IllegalArgumentException("Nan.");
        } else if (inputValue == Double.NEGATIVE_INFINITY) {
            thousandths = Integer.MIN_VALUE;
        } else if (inputValue == Double.POSITIVE_INFINITY) {
            thousandths = Integer.MAX_VALUE;
        } else {
            thousandths = (int) Math.round(inputValue * 1000);
        }
    }

    public static Influence none() {
        return new Influence(0);
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

    public int compareTo(Number number) {
        return Double.compare(getValue(), number.doubleValue()); //very sad that you can't do shift operations on doubles :(
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

    public Influence reverseModifier(double modifier) {
        return new Influence((int) Math.ceil(getValue() / modifier * 1000));
    }

    /**
     *
     * @param modifier
     * @return The amount of value rounded off when applying this modifier.
     */
    @NotNull
    public Influence remainderOfModifier(double modifier) {
        Influence resultingInfluence = applyModifier(modifier);
        return new Influence(getValue() - resultingInfluence.getValue() / modifier);
    }

    public boolean isNegative() {
        return thousandths < 0;
    }

    public boolean getNonZero() {
        return thousandths != 0;
    }

    public boolean greaterThan(Influence influence) {
        return compareTo(influence) > 0;
    }

    @NotNull
    public Influence plus(@Nullable Influence request) {
        return add(request);
    }
}
