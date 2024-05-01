package today.tecktip.killbill.common.gameserver.data;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents coordinates in the game.
 * @param x X coordinate
 * @param y Y coordinate
 */
public record Coordinates(@JsonProperty("x") double x, @JsonProperty("y") double y) {


    /**
     * Copies to a new instance.
     * @return New duplicate instance
     */
    public Coordinates copy() {
        return new Coordinates(x, y);
    }

    /**
     * Updates the X coordinate.
     * @param newX New X coordinate
     * @return New coordinates
     */
    public Coordinates withX(final double newX) {
        return new Coordinates(newX, y);
    }

    /**
     * Updates the Y coordinate.
     * @param newY New Y coordinate
     * @return New coordinates
     */
    public Coordinates withY(final double newY) {
        return new Coordinates(x, newY);
    }

    /**
     * Adds an offset to the X coordinate.
     * @param offset Offset to add to X
     * @return New coordinates
     */
    public Coordinates offsetX(final double offset) {
        return new Coordinates(x + offset, y);
    }

    /**
     * Adds an offset to the Y coordinate.
     * @param offset Offset to add to Y
     * @return New coordinates
     */
    public Coordinates offsetY(final double offset) {
        return new Coordinates(x, y + offset);
    }

    /**
     * Adds an offset to the X and Y coordinates.
     * @param offsetX Offset to add to X
     * @param offsetY Offset to add to Y
     * @return New coordinates
     */
    public Coordinates offsetXY(final double offsetX, final double offsetY) {
        return new Coordinates(x + offsetX, y + offsetY);
    }

    /**
     * Returns a list representation of these coordinates.
     * @return List representation, {x, y}
     */
    public List<Double> toList() {
        ArrayList<Double> intCoords = new ArrayList<>();
        intCoords.add(x);
        intCoords.add(y);
        return intCoords;
    }

    /**
     * Parses coordinates from a list of two double (0=x, 1=y).
     * @param coords Coordinate list (2 values)
     * @return New coordinates
     */
    public static Coordinates fromList(List<Double> coords) {
        // Must have two values
        if (coords.size() != 2) throw new IllegalArgumentException("Coordinates must have two items");

        return new Coordinates(coords.get(0), coords.get(1));
    }

    /**
     * Parses coordinates from a string.
     * @param value X,Y or XxY
     * @return Tile coordinates matching string value
     */
    public static Coordinates fromString(final String value) {
        // Split on , or x
        final String[] vals;
        if (value.contains(",")) {
            vals = value.split(",");
        }
        else if (value.contains("x")) {
            vals = value.split("x", 2);
        }
        else {
            throw new IllegalArgumentException("Invalid coordinates. Must be _,_ or _x_.");
        }

        if (vals.length != 2) {
            throw new IllegalArgumentException("Invalid coordinates. Must be _,_ or _x_.");
        }



        // Try to make ints
        double x;
        double y;
        try {
            // Each coordinate may be gridscale, if it includes a +/-.
            if (vals[0].contains("/")) x = parseGridscale(vals[0]);
            else x = Double.valueOf(vals[0]);

            if (vals[1].contains("/")) y = parseGridscale(vals[1]);
            else y = Double.valueOf(vals[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid coordinates. " + e.getMessage());
        }

        return new Coordinates(x, y);
    }

    /**
     * Parses gridscale components, such as:
     * <code>
     * 1+1/3  // 1.3333
     * 3-1/2  // 2.5
     * 1-6/20 // 0.7
     * </code>
     * @param value String value 
     * @return Double value
     */
    public static double parseGridscale(final String value) {
        // Plus or minus?
        int mult;
        String[] vals;
        if (value.contains("+")) {
            vals = value.split("\\+");
            mult = 1;
        } else if (value.contains("-")) {
            vals = value.split("-");
            mult = -1;
        } else {
            vals = new String[] {"", value};
            mult = 1;
        }

        if (vals.length < 2) {
            throw new IllegalArgumentException("Invalid gridscale value: " + value + " (missing right argument)");
        }

        // Now there should be a slash
        if (!vals[vals.length - 1].contains("/")) {
            throw new IllegalArgumentException("Invalid gridscale value: " + value + " (fractional part is not a fraction)");
        }

        String[] fractionalVals = vals[vals.length - 1].split("\\/");
        if (fractionalVals.length != 2) {
            throw new IllegalArgumentException("Invalid gridscale value: " + value + " (fractional part is missing left or right argument)");
        }

        // Parse both left and right as ints
        int numerator;
        int denominator;
        try {
            numerator = Integer.valueOf(fractionalVals[0]);
            denominator = Integer.valueOf(fractionalVals[1]);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Invalid gridscale value: " + value + " (fractional parts are not integers)");
        }

        // Parse the integer component
        int integerComponent;
        String integerVal = String.join(mult == 1 ? "+" : "-", Arrays.copyOfRange(vals, 0, vals.length - 1));
        if (integerVal.strip().length() == 0) {
            integerComponent = 0;
        } else {
            try {
                integerComponent = Integer.valueOf(integerVal);
            } catch (final Exception e) {
                throw new IllegalArgumentException("Invalid gridscale value: " + value + " (could not parse whole component as an integer)");
            }
        }

        // Ready to go.
        return integerComponent + mult * ((double) numerator / (double) denominator);
    }
}
