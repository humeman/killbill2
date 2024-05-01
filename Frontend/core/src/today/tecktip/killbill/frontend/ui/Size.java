package today.tecktip.killbill.frontend.ui;

import today.tecktip.killbill.frontend.KillBillGame;

/**
 * Represents a scalable size on the UI.
 * @author cs
 */
public abstract class Size {
    /**
     * Gets the width.
     * @return Width in pixels
     */
    public abstract float getWidth();

    /**
     * Gets the height.
     * @return Height in pixels
     */
    public abstract float getHeight();

    @Override
    public String toString() {
        return "[" + getWidth() + "," + getHeight() + "]";
    }

    /**
     * Represents a fixed size, regardless of the screen's pixels.
     */
    public static class FixedSize extends Size {
        /**
         * Width
         */
        private float width;

        /**
         * Height
         */
        private float height;
        
        /**
         * Constructs a new fixed size.
         * @param width Width in pixels
         * @param height Height in pixels
         */
        public FixedSize(float width, float height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public float getWidth() {
            return width;
        }

        @Override
        public float getHeight() {
            return height;
        }

        /**
         * Changes the width.
         * @param width New width in pixels
         * @return This instance for chaining
         */
        public FixedSize setWidth(float width) {
            this.width = width;
            return this;
        }

        /**
         * Changes the Y coordinate.
         * @param height New height in pixels
         * @return This instance for chaining
         */
        public FixedSize setHeight(float height) {
            this.height = height;
            return this;
        }

        /**
         * Offsets the width.
         * @param delta Pixels to add to the width
         * @return This instance for chaining
         */
        public FixedSize offsetWidth(float delta) {
            this.width += delta;
            return this;
        }

        /**
         * Offsets the height.
         * @param delta Pixels to add to the height
         * @return This instance for chaining
         */
        public FixedSize offsetHeight(float delta) {
            this.height += delta;
            return this;
        }
    }

    /**
     * Represents an object size consistent across screen sizes.
     */
    public static class ScaledSize extends Size {
        /**
         * Width factor
         */
        private float widthFactor;

        /**
         * Height factor
         */
        private float heightFactor;
        
        /**
         * Constructs a new scaled size. Each factor represents its width on the screen, where
         *  0 is the smallest size and 1 is the entire size.
         * @param widthFactor Width factor (0 to 1)
         * @param heightFactor Height factor (0 to 1)
         */
        public ScaledSize(float widthFactor, float heightFactor) {
            this.widthFactor = widthFactor;
            this.heightFactor = heightFactor;
        }

        @Override
        public float getWidth() {
            return KillBillGame.get().getWidth() * widthFactor;
        }

        @Override
        public float getHeight() {
            return KillBillGame.get().getHeight() * heightFactor;
        }

        /**
         * Changes the width.
         * @param widthFactor New width factor
         * @return This instance for chaining
         */
        public ScaledSize setWidth(float widthFactor) {
            this.widthFactor = widthFactor;
            return this;
        }

        /**
         * Changes the height.
         * @param heightFactor New height factor
         * @return This instance for chaining
         */
        public ScaledSize setHeight(float heightFactor) {
            this.heightFactor = heightFactor;
            return this;
        }

        /**
         * Offsets the width.
         * @param delta Factor to add to width
         * @return This instance for chaining
         */
        public ScaledSize offsetWidth(float delta) {
            this.widthFactor += delta;
            return this;
        }

        /**
         * Offsets the height.
         * @param delta Factor to add to height
         * @return This instance for chaining
         */
        public ScaledSize offsetHeight(float delta) {
            this.heightFactor += delta;
            return this;
        }

        /**
         * Scales the width. For example, 0.5 scaled by 0.5 yields 0.25.
         * @param scale Scale to multiply width by
         * @return This instance for chaining
         */
        public ScaledSize scaleWidth(float scale) {
            this.widthFactor *= scale;
            return this;
        }

        /**
         * Scales the height. For example, 0.5 scaled by 0.5 yields 0.25.
         * @param scale Scale to multiply height by
         * @return This instance for chaining
         */
        public ScaledSize scaleHeight(float scale) {
            this.heightFactor *= scale;
            return this;
        }
    }

    /**
     * Represents an object size consistent across screen sizes, scaled only by the width.
     */
    public static class XScaledSize extends Size {
        /**
         * Width factor
         */
        private float widthFactor;

        /**
         * Height factor
         */
        private float heightFactor;
        
        /**
         * Constructs a new scaled size. Each factor represents its width on the screen, where
         *  0 is the smallest size and 1 is the entire size.
         * @param widthFactor Width factor (0 to 1)
         * @param heightFactor Height factor (will be multiplied by X for the resulting height).
         */
        public XScaledSize(float widthFactor, float heightFactor) {
            this.widthFactor = widthFactor;
            this.heightFactor = heightFactor;
        }

        @Override
        public float getWidth() {
            return KillBillGame.get().getWidth() * widthFactor;
        }

        @Override
        public float getHeight() {
            return getWidth() * heightFactor;
        }

        /**
         * Changes the width.
         * @param widthFactor New width factor
         * @return This instance for chaining
         */
        public XScaledSize setWidth(float widthFactor) {
            this.widthFactor = widthFactor;
            return this;
        }

        /**
         * Changes the height.
         * @param heightFactor New height factor
         * @return This instance for chaining
         */
        public XScaledSize setHeight(float heightFactor) {
            this.heightFactor = heightFactor;
            return this;
        }

        /**
         * Offsets the width.
         * @param delta Factor to add to width
         * @return This instance for chaining
         */
        public XScaledSize offsetWidth(float delta) {
            this.widthFactor += delta;
            return this;
        }

        /**
         * Offsets the height.
         * @param delta Factor to add to height
         * @return This instance for chaining
         */
        public XScaledSize offsetHeight(float delta) {
            this.heightFactor += delta;
            return this;
        }

        /**
         * Scales the width. For example, 0.5 scaled by 0.5 yields 0.25.
         * @param scale Scale to multiply width by
         * @return This instance for chaining
         */
        public XScaledSize scaleWidth(float scale) {
            this.widthFactor *= scale;
            return this;
        }

        /**
         * Scales the height. For example, 0.5 scaled by 0.5 yields 0.25.
         * @param scale Scale to multiply height by
         * @return This instance for chaining
         */
        public XScaledSize scaleHeight(float scale) {
            this.heightFactor *= scale;
            return this;
        }
    }

    /**
     * Represents an object size consistent across screen sizes, scaled only by the height.
     */
    public static class YScaledSize extends Size {
        /**
         * Width factor
         */
        private float widthFactor;

        /**
         * Height factor
         */
        private float heightFactor;
        
        /**
         * Constructs a new scaled size. Each factor represents its width on the screen, where
         *  0 is the smallest size and 1 is the entire size.
         * @param widthFactor Width factor (will be multiplied by Y for the resulting height).
         * @param heightFactor Height factor (0 to 1)
         */
        public YScaledSize(float widthFactor, float heightFactor) {
            this.widthFactor = widthFactor;
            this.heightFactor = heightFactor;
        }

        @Override
        public float getWidth() {
            return getHeight() * widthFactor;
        }

        @Override
        public float getHeight() {
            return KillBillGame.get().getHeight() * heightFactor;
        }

        /**
         * Changes the width.
         * @param widthFactor New width factor
         * @return This instance for chaining
         */
        public YScaledSize setWidth(float widthFactor) {
            this.widthFactor = widthFactor;
            return this;
        }

        /**
         * Changes the height.
         * @param heightFactor New height factor
         * @return This instance for chaining
         */
        public YScaledSize setHeight(float heightFactor) {
            this.heightFactor = heightFactor;
            return this;
        }

        /**
         * Offsets the width.
         * @param delta Factor to add to width
         * @return This instance for chaining
         */
        public YScaledSize offsetWidth(float delta) {
            this.widthFactor += delta;
            return this;
        }

        /**
         * Offsets the height.
         * @param delta Factor to add to height
         * @return This instance for chaining
         */
        public YScaledSize offsetHeight(float delta) {
            this.heightFactor += delta;
            return this;
        }

        /**
         * Scales the width. For example, 0.5 scaled by 0.5 yields 0.25.
         * @param scale Scale to multiply width by
         * @return This instance for chaining
         */
        public YScaledSize scaleWidth(float scale) {
            this.widthFactor *= scale;
            return this;
        }

        /**
         * Scales the height. For example, 0.5 scaled by 0.5 yields 0.25.
         * @param scale Scale to multiply height by
         * @return This instance for chaining
         */
        public YScaledSize scaleHeight(float scale) {
            this.heightFactor *= scale;
            return this;
        }
    }
}
