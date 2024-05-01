package today.tecktip.killbill.common.gameserver.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents tile coordinates in the game.
 * @param x X coordinate (int)
 * @param y Y coordinate (int)
 */
public record TileCoordinates(@JsonProperty("x") int x, @JsonProperty("y") int y) {


    /**
     * Copies to a new instance.
     * @return New duplicate instance
     */
    public TileCoordinates copy() {
        return new TileCoordinates(x, y);
    }

    /**
     * Updates the X coordinate.
     * @param newX New X coordinate
     * @return New coordinates
     */
    public TileCoordinates withX(final int newX) {
        return new TileCoordinates(newX, y);
    }

    /**
     * Updates the Y coordinate.
     * @param newY New Y coordinate
     * @return New coordinates
     */
    public TileCoordinates withY(final int newY) {
        return new TileCoordinates(x, newY);
    }

    /**
     * Adds an offset to the X coordinate.
     * @param offset Offset to add to X
     * @return New coordinates
     */
    public TileCoordinates offsetX(final int offset) {
        return new TileCoordinates(x + offset, y);
    }

    /**
     * Adds an offset to the Y coordinate.
     * @param offset Offset to add to Y
     * @return New coordinates
     */
    public TileCoordinates offsetY(final int offset) {
        return new TileCoordinates(x, y + offset);
    }

    /**
     * Adds an offset to the X and Y coordinates.
     * @param offsetX Offset to add to X
     * @param offsetY Offset to add to Y
     * @return New coordinates
     */
    public TileCoordinates offsetXY(final int offsetX, final int offsetY) {
        return new TileCoordinates(x + offsetX, y + offsetY);
    }

    /**
     * Returns a list representation of these coordinates.
     * @return List representation, {x, y}
     */
    public List<Integer> toList() {
        ArrayList<Integer> intCoords = new ArrayList<>();
        intCoords.add(x);
        intCoords.add(y);
        return intCoords;
    }

    /**
     * Parses coordinates from a list of two ints (0=x, 1=y).
     * @param coords Coordinate list (2 values)
     * @return New coordinates
     */
    public static TileCoordinates fromList(List<Integer> coords) {
        // Must have two values
        if (coords.size() != 2) throw new IllegalArgumentException("Coordinates must have two items");

        return new TileCoordinates(coords.get(0), coords.get(1));
    }

    /**
     * Parses tile coordinates from a string.
     * @param value X,Y or XxY
     * @return Tile coordinates matching string value
     */
    public static TileCoordinates fromString(final String value) {
        // Split on , or x
        final String[] vals;
        if (value.contains(",")) {
            vals = value.split(",", 2);
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
        int x;
        int y;
        try {
            x = Integer.valueOf(vals[0]);
            y = Integer.valueOf(vals[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid coordinates. Could not be parsed as integers");
        }

        return new TileCoordinates(x, y);
    }
}