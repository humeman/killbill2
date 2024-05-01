package today.tecktip.killbill.frontend.ui;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.config.GlobalGameConfig;

/**
 * Represents a scalable location on the UI.
 * @author cs
 */
public abstract class Location {
    /**
     * Gets the X coordinate for this location.
     * @return X coordinate
     */
    public abstract float getX();

    /**
     * Gets the Y coordinate for this location.
     * @return Y coordinate in tiles
     */
    public abstract float getY();

    /**
     * Offsets the X coordinate.
     * @param <T> Size type
     * @param x X coordinate offset in pixels
     */
    public abstract <T extends Location> T offsetX(final float x);

    /**
     * Offsets the Y coordinate.
     * @param <T> Size type
     * @param y Y coordinate offset in pixels
     */
    public abstract <T extends Location> T offsetY(final float y);

    /**
     * Sets the X coordinate.
     * @param <T> Size type
     * @param x X coordinate in pixels
     */
    public abstract <T extends Location> T setX(final float x);

    /**
     * Sets the Y coordinate.
     * @param <T> Size type
     * @param y Y coordinate in pixels
     */
    public abstract <T extends Location> T setY(final float y);

    /**
     * Gets the X coordinate for this location.
     * @return X coordinate in tiles
     */
    public abstract float getTileX();

    /**
     * Gets the Y coordinate for this location.
     * @return Y coordinate in tiles
     */
    public abstract float getTileY();

    /**
     * Sets the X coordinate in tiles.
     * @param <T> Size type
     * @param xTiles X coordinate in tiles
     */
    public abstract <T extends Location> T setTileX(final float xTiles);

    /**
     * Sets the Y coordinate in tiles.
     * @param <T> Size type
     * @param yTiles Y coordinate in tiles
     */
    public abstract <T extends Location> T setTileY(final float yTiles);

    /**
     * Returns a distinct copy of this size.
     * @param <T> Size type
     * @return Copy of size
     */
    public abstract <T extends Location> T copy();

    @Override
    public String toString() {
        return "[" + getX() + "," + getY() + "]";
    }

    /**
     * Represents a fixed location in space, regardless of the screen's pixels.
     */
    public static class FixedLocation extends Location {
        /**
         * X location
         */
        private float x;

        /**
         * Y location
         */
        private float y;

        /**
         * If true, the X coordinate and Y coordinate are equal.
         */
        private boolean coordinatesMatch;
        
        /**
         * Constructs a new fixed location.
         * @param x X coordinate
         * @param y Y coordinate
         */
        public FixedLocation(float x, float y) {
            this.x = x;
            this.y = y;
            coordinatesMatch = false;
        }

        /**
         * Constructs a new fixed location where both coordinates match.
         * @param x The X and Y coordinate.
         */
        public FixedLocation(float x) {
            this.x = x;
            this.y = 0;
            coordinatesMatch = true;
        }

        @Override
        public float getX() {
            return x;
        }

        @Override
        public float getY() {
            if (coordinatesMatch) return x;
            return y;
        }

        @Override
        public float getTileX() {
            return getX() / GlobalGameConfig.GRID_SIZE;
        }

        @Override
        public float getTileY() {
            return getY() / GlobalGameConfig.GRID_SIZE;
        }

        @Override
        @SuppressWarnings("unchecked")
        public FixedLocation setX(float x) {
            this.x = x;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public FixedLocation setY(float y) {
            this.y = y;
            coordinatesMatch = false;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public FixedLocation setTileX(float xTiles) {
            setX(xTiles * GlobalGameConfig.GRID_SIZE);
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public FixedLocation setTileY(float yTiles) {
            setX(yTiles * GlobalGameConfig.GRID_SIZE);
            coordinatesMatch = false;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public FixedLocation offsetX(float delta) {
            this.x += delta;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public FixedLocation offsetY(float delta) {
            if (coordinatesMatch) y = x + delta;
            else this.y += delta;
            coordinatesMatch = false;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public FixedLocation copy() {
            return new FixedLocation(x, y);
        }
    }

    /**
     * Represents a location in space consistent across screen sizes.
     */
    public static class ScaledLocation extends Location {
        /**
         * X factor
         */
        private float xFactor;

        /**
         * Y factor
         */
        private float yFactor;

        /**
         * If true, the X coordinate will be used for X and Y.
         */
        private boolean coordinatesMatch;
        
        /**
         * Constructs a new scaled location. Each factor represents its position on the screen, where
         *  0 is the lowest point and 1 is the highest point.
         * @param xFactor X factor (0 to 1)
         * @param yFactor Y factor (0 to 1)
         */
        public ScaledLocation(float xFactor, float yFactor) {
            this.xFactor = xFactor;
            this.yFactor = yFactor;
            coordinatesMatch = false;
        }

        /**
         * Constructs a new scaled location where the X and Y coordinates match. 
         * Each factor represents its position on the screen, where 0 is the 
         *  lowest point and 1 is the highest point.
         * @param xFactor X factor (0 to 1)
         */
        public ScaledLocation(float xFactor) {
            this.xFactor = xFactor;
            this.yFactor = 0;
            coordinatesMatch = true;
        }

        @Override
        public float getX() {
            return KillBillGame.get().getWidth() * xFactor;
        }

        @Override
        public float getY() {
            if (coordinatesMatch) return getX();
            return KillBillGame.get().getHeight() * yFactor;
        }

        @Override
        public float getTileX() {
            return getX() / GlobalGameConfig.GRID_SIZE;
        }

        @Override
        public float getTileY() {
            return getY() / GlobalGameConfig.GRID_SIZE;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ScaledLocation setX(float x) {
            this.xFactor = x / KillBillGame.get().getWidth();
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ScaledLocation setY(float y) {
            this.yFactor = y / KillBillGame.get().getHeight();
            coordinatesMatch = false;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ScaledLocation setTileX(float xTiles) {
            setX(xTiles * GlobalGameConfig.GRID_SIZE);
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ScaledLocation setTileY(float yTiles) {
            setX(yTiles * GlobalGameConfig.GRID_SIZE);
            coordinatesMatch = false;
            return this;
        }

        /**
         * Changes the X factor.
         * @param xFactor New X factor
         * @return This instance for chaining
         */
        public ScaledLocation setXScaled(float xFactor) {
            this.xFactor = xFactor;
            return this;
        }

        /**
         * Changes the Y coordinate.
         * @param yFactor New Y factor
         * @return This instance for chaining
         */
        public ScaledLocation setYScaled(float yFactor) {
            this.yFactor = yFactor;
            coordinatesMatch = false;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ScaledLocation offsetX(float delta) {
            this.xFactor += delta / KillBillGame.get().getWidth();
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ScaledLocation offsetY(float delta) {
            if (coordinatesMatch) this.yFactor = xFactor + delta / KillBillGame.get().getWidth();
            else this.yFactor += delta / KillBillGame.get().getHeight();
            coordinatesMatch = false;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ScaledLocation copy() {
            if (coordinatesMatch) return new ScaledLocation(xFactor);
            else return new ScaledLocation(xFactor, yFactor);
        }

        /**
         * Offsets the X factor.
         * @param delta Factor to add to X factor
         * @return This instance for chaining
         */
        public ScaledLocation offsetXScale(float delta) {
            this.xFactor += delta;
            return this;
        }

        /**
         * Offsets the Y factor.
         * @param delta Factor to add to Y factor
         * @return This instance for chaining
         */
        public ScaledLocation offsetYScale(float delta) {
            if (coordinatesMatch) this.yFactor = xFactor + delta;
            else this.yFactor += delta;
            coordinatesMatch = false;
            return this;
        }

        /**
         * Scales the X factor. For example, 0.5 scaled by 0.5 yields 0.25.
         * @param scale Scale to multiply X factor by
         * @return This instance for chaining
         */
        public ScaledLocation scaleX(float scale) {
            this.xFactor *= scale;
            return this;
        }

        /**
         * Scales the Y factor. For example, 0.5 scaled by 0.5 yields 0.25.
         * @param scale Scale to multiply Y factor by
         * @return This instance for chaining
         */
        public ScaledLocation scaleY(float scale) {
            if (coordinatesMatch) yFactor = xFactor * scale;
            else this.yFactor *= scale;
            coordinatesMatch = false;
            return this;
        }
    }
}
