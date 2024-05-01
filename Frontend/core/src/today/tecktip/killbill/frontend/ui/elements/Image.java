package today.tecktip.killbill.frontend.ui.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.ui.Location;
import today.tecktip.killbill.frontend.ui.Rectangle;
import today.tecktip.killbill.frontend.ui.Size;
import today.tecktip.killbill.frontend.ui.UiElement;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.Size.FixedSize;

/**
 * Represents an image on the UI.
 * @author cs
 */
public class Image implements UiElement {
    
    /**
     * Constructs a new builder for an image.
     * @return New Image builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Visibility state
     */
    private boolean visible;

    /**
     * Texture this image is drawing.
     */
    private Texture texture;

    /**
     * Rectangle where the image is drawn.
     */
    private Rectangle rectangle;

    /**
     * The tint to apply to the image, if wanted.
     */
    private Color tint = null;

    /**
     * Constructs a new Image. Designed for use with the {@link Builder}.
     * @param texture Texture to display
     * @param rectangle Rectangle to display in
     */
    private Image(
        final Texture texture,
        final Rectangle rectangle
    ) {
        this.texture = texture;
        this.rectangle = rectangle;
        visible = true;
    }

    @Override
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    /**
     * Changes the tint to apply to this image.
     * @param tint New tint
     */
    public void setTint(final Color tint) {
        this.tint = tint;
    }

    /**
     * Checks if the image uses tint.
     * @return True if tint supplied
     */
    public boolean hasTint() {
        return tint != null;
    }

    /**
     * Gets the tint color to apply to the image.
     * @return Tint color
     */
    public Color getTint() {
        return tint;
    }

    /**
     * Changes the image's texture.
     * @param texture New texture
     */
    public void setTexture(final Texture texture) {
        this.texture = texture;
    }

    @Override
    public void render(final SpriteBatch batch, final float delta) {
        Color oldColor = null;
        if (tint != null) {
            oldColor = batch.getColor().cpy();
            batch.setColor(tint.cpy());
        }

        batch.draw(texture, rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());

        if (tint != null) {
            batch.setColor(oldColor);
        }
    }

    @Override
    public boolean onClicked() {
        // We don't really want anything to happen here
        return false;
    }

    @Override
    public boolean onUnclicked() {
        // Or here
        return false;
    }

    @Override
    public boolean onKeyPressed(final int key) {
        // Or here
        return false;
    }

    @Override
    public boolean onKeyTyped(final char c) {
        // Or here
        return false;
    }

    @Override
    public Rectangle getRectangle() {
        return rectangle;
    }

    @Override
    public void onResize() {}

    /**
     * Builds a new Image.
     */
    public static class Builder {
        /**
         * Texture name
         */
        private String texture;

        /**
         * Location to display at
         */
        private Location location;

        /**
         * Size of image
         */
        private Size size;
        
        /**
         * Tint color
         */
        private Color tint;

        /**
         * Makes a new Builder with default values.
         */
        public Builder() {
            texture = "you_forgot_to_set_the_image_texture";
            location = new FixedLocation(0, 0);
            size = new FixedSize(100, 100);
            tint = null;
        }

        /**
         * Sets the texture to display with this image.
         * @param name Texture name
         * @return Builder for chaining
         */
        public Builder setTexture(final String name) {
            this.texture = name;
            return this;
        }

        /**
         * Changes the location of the image.
         * @param location Image location
         * @return Builder for chaining
         */
        public Builder setLocation(final Location location) {
            this.location = location;
            return this;
        }

        /**
         * Changes the size of the image.
         * @param size Image size
         * @return Builder for chaining
         */
        public Builder setSize(final Size size) {
            this.size = size;
            return this;
        }

        /**
         * Changes the tint of the image.
         * @param tint Tint color to apply
         * @return Builder for chaining
         */
        public Builder setTint(final Color tint) {
            this.tint = tint;
            return this;
        }

        /**
         * Builds an Image out of the parameters supplied.
         * @return New image
         */
        public Image build() {
            final Image image = new Image(
                KillBillGame.get().getTextureLoader().get(texture),
                new Rectangle(location, size)
            );

            if (tint != null) {
                image.setTint(tint);
            }

            return image;
        }
    }
}
