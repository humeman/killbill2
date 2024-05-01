package today.tecktip.killbill.frontend.ui;

/**
 * Represents a rectangle with an X/Y coordinate and size.
 * @author cs
 */
public class Rectangle {
    /**
     * This rectangle's lowest coordinate.
     */
    private Location location;

    /**
     * This rectangle's size.
     */
    private Size size;
    
    /**
     * Constructs a new rectangle.
     * @param location X/Y location (lowest X and Y coordinates)
     * @param size Width/height
     */
    public Rectangle(final Location location, final Size size) {
        this.location = location;
        this.size = size;
    }

    /**
     * Gets the lowest coordinate point for this rectangle.
     * @return Lowest X and Y coordinates
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Gets the size of this rectangle.
     * @return Width + height
     */
    public Size getSize() {
        return size;
    }

    /**
     * Gets the X coordinate of the image. Shortcut to {@link Location#getX()}.
     * @return X coordinate (pixels)
     */
    public float getX() {
        return location.getX();
    }

    /**
     * For Caleb
     * @return X coordinate (pixels)
     */
    public float getTheHorizontalComponentOfTheRectanglesLocationWhichMeansTheSideToSideComponent() {
        return location.getX();
    }

    /**
     * Gets the Y coordinate of the image. Shortcut to {@link Location#getY()}.
     * @return Y coordinate (pixels)
     */
    public float getY() {
        return location.getY();
    }

    /**
     * For Caleb
     * @return Y coordinate (pixels)
     */
    public float getTheVerticalComponentOfTheRectanglesLocationWhichMeansTheUpAndDownComponent() {
        return location.getY();
    }

    /**
     * Gets the width of the image. Shortcut to {@link Size#getWidth()}.
     * @return Width (pixels)
     */
    public float getWidth() {
        return size.getWidth();
    }

    /**
     * Gets the height of the image. Shortcut to {@link Size#getHeight()()}.
     * @return Height (pixels)
     */
    public float getHeight() {
        return size.getHeight();
    }

    /**
     * Gets the center X coordinate.
     * @return Center X
     */
    public float getCenterX() {
        return location.getX() + getWidth() / 2;
    }

    /**
     * Gets the center Y coordinate.
     * @return Center Y
     */
    public float getCenterY() {
        return location.getY() + getHeight() / 2;
    }

    /**
     * Checks if a point is contained in this rectangle.
     * @param x X coordinate (pixels)
     * @param y Y coordinate (pixels)
     * @return True if the point is within the rectangle
     */
    public boolean containsPoint(float x, float y) {
        // Find low and high X
        float lowX = location.getX();
        float highX = lowX + size.getWidth();

        float lowY = location.getY();
        float highY = lowY + size.getHeight();

        return (x >= lowX && x < highX && y >= lowY && y < highY);
    }

    /**
     * Returns true if any point of this rectangle overlaps the other rectangle.
     * @param otherRect
     * @return
     */
    public boolean overlaps(final Rectangle otherRect) {
        // If the rectangle overlaps, any of the four corners of the other rectangle will be contained in this one
        return otherRect.getX() < getX() + getWidth() && otherRect.getX() + otherRect.getWidth() > getX()
            && otherRect.getY() < getY() + getHeight() && otherRect.getY() + otherRect.getHeight() > getY();
    }

    public boolean overlapsAt(final Rectangle otherRect, final float x, final float y) {
        // Well this is great
        float oldX = getX();
        float oldY = getY();
        getLocation().setX(x);
        getLocation().setY(y);
        boolean ov = overlaps(otherRect);
        getLocation().setX(oldX);
        getLocation().setY(oldY);
        return ov;
    }

    @Override
    public String toString() {
        return "location=" + location + ", size=" + size;
    }
}
