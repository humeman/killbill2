package today.tecktip.killbill.frontend.ui.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.resources.FontLoader;
import today.tecktip.killbill.frontend.ui.Location;
import today.tecktip.killbill.frontend.ui.Rectangle;
import today.tecktip.killbill.frontend.ui.Size;
import today.tecktip.killbill.frontend.ui.UiElement;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.Size.FixedSize;

/**
 * Represents a text label on the UI.
 * @author cs
 */
public class Label implements UiElement {
    
    /**
     * Constructs a new builder for a label.
     * @return New Label builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Visibility state
     */
    private boolean visible;

    /**
     * Centered on coordinates or not
     */
    private boolean centered;

    /**
     * Location where the text is drawn.
     */
    private Rectangle rectangle;

    /**
     * Base location before any scaling is applied.
     */
    private Location baseLocation;

    /**
     * The font this label is rendering with.
     */
    private BitmapFont font;

    /**
     * The builder that generates fonts for this label.
     */
    private FontLoader.Builder fontBuilder;

    /**
     * The label's text.
     */
    private String text;

    /**
     * Tint color.
     */
    private Color tint;

    /**
     * Constructs a new Label. Designed for use with the {@link Builder}.
     * @param location Coordinates to draw the label at
     * @param fontBuilder Builder to generate the font to render with
     * @param text The text to draw
     * @param centered If true, text is centered on the supplied X and Y coordinates
     */
    private Label(
        final Location location,
        final FontLoader.Builder fontBuilder,
        final String text,
        final boolean centered
    ) {
        baseLocation = location.copy();
        this.fontBuilder = fontBuilder;
        this.text = text;
        this.centered = centered;
        this.tint = null;
        visible = true;
        updateFonts(true);
    }

    @Override
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void render(final SpriteBatch batch, final float delta) {
        Color oldColor = null;
        if (tint != null) {
            oldColor = font.getColor().cpy();
            font.setColor(tint);
        }

        font.draw(batch, text, centered ? rectangle.getX() : baseLocation.getX(), centered ? rectangle.getY() : baseLocation.getY());

        if (oldColor != null) {
            font.setColor(oldColor);
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

    /**
     * Gets the base location where this label renders. If you update it, call {@link #updateFonts(boolean)} with `false`.
     * @return Location
     */
    public Location getLocation() {
        return baseLocation;
    }

    @Override
    public void onResize() {
        updateFonts(true);
    }

    /**
     * Updates the font used for rendering.
     */
    public void updateFonts(boolean recreate) {
        if (fontBuilder.isScaled()) {
            if (recreate) {
                if (font != null)
                    font = fontBuilder.refreshScale().build();
                else
                    font = fontBuilder.build();
            }
            if (centered) {
                final Size size = calculateFontSize();
                final Location newLocation = baseLocation.copy();
                newLocation.offsetX(size.getWidth() / -2f);
                newLocation.offsetY(size.getHeight() / 2f);

                rectangle = new Rectangle(
                    newLocation,
                    size
                );
            }
        } else {
            if (recreate)
                font = fontBuilder.build();
            if (centered) {
                final Size size = calculateFontSize();

                rectangle = new Rectangle(
                    baseLocation,
                    size
                );
            }
        }
    }

    /**
     * Calculates the size of this message.
     * @return A size for this message
     */
    private Size calculateFontSize() {
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, text);

        return new FixedSize(layout.width, layout.height);
    }

    /**
     * Changes the text of this label.
     * @param text New text content
     */
    public void setText(final String text) {
        this.text = text;
        updateFonts(false);
    }

    /**
     * Gets the current text of the label.
     * @return Text
     */
    public String getText() {
        return text;
    }

    /**
     * Changes the tint of this label.
     * @param tint New tint
     */
    public void setTint(final Color tint) {
        this.tint = tint;
    }

    /**
     * Changes the tint of this label.
     * @return Font tint
     */
    public Color getTint() {
        return tint;
    }

    /**
     * Builds a new Label.
     */
    public static class Builder {
        /**
         * Font generator
         */
        private FontLoader.Builder fontBuilder;

        /**
         * Location to display at
         */
        private Location location;
        
        /**
         * Label's text
         */
        private String text;

        /**
         * Whether or not the label is centered
         */
        private boolean centered;

        /**
         * Tint to use while drawing.
         */
        private Color tint;

        /**
         * Makes a new Builder with default values.
         */
        public Builder() {
            fontBuilder = null;
            location = new FixedLocation(0, 0);
            text = "You forgot to set some text for this label. Fool!";
            tint = null;
            centered = true;
        }

        /**
         * Changes the location of the label.
         * @param location Label location
         * @return Builder for chaining
         */
        public Builder setLocation(final Location location) {
            this.location = location;
            return this;
        }

        /**
         * Sets the font builder for the label.
         * @param fontBuilder Font builder
         * @return Builder for chaining
         */
        public Builder setFontBuilder(final FontLoader.Builder fontBuilder) {
            this.fontBuilder = fontBuilder;
            return this;
        }

        /**
         * Sets the text for the label.
         * @param text Text to draw
         * @return Builder for chaining
         */
        public Builder setText(final String text) {
            this.text = text;
            return this;
        }

        /**
         * Defines if the text should be centered at the specified coordinates or not.
         * @param centered If true, text is centered on X and Y
         * @return Builder for chaining
         */
        public Builder setCentered(final boolean centered) {
            this.centered = centered;
            return this;
        }

        /**
         * Changes the tint of this label.
         * @param tint New tint color
         * @return Builder for chaining
         */
        public Builder setTint(final Color tint) {
            this.tint = tint;
            return this;
        }

        /**
         * Builds a Label out of the parameters supplied.
         * @return New label
         */
        public Label build() {
            if (fontBuilder == null) {
                throw new CatastrophicException("A font builder is required before a Label can be built.");
            }

            final Label label = new Label(
                location,
                fontBuilder,
                text,
                centered
            );

            if (tint != null) label.setTint(tint);

            return label;
        }
    }
}
