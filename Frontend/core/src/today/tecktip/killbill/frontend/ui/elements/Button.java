package today.tecktip.killbill.frontend.ui.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.resources.FontLoader;
import today.tecktip.killbill.frontend.ui.Location;
import today.tecktip.killbill.frontend.ui.Rectangle;
import today.tecktip.killbill.frontend.ui.Size;
import today.tecktip.killbill.frontend.ui.UiElement;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.Size.FixedSize;

/**
 * Represents a button on the UI.
 * @author cs
 */
public class Button implements UiElement {
    
    /**
     * Constructs a new builder for a button.
     * @return New Button builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Visibility state
     */
    private boolean visible;

    /**
     * Texture this button is drawing.
     */
    private Texture texture;

    /**
     * Rectangle where the button is drawn.
     */
    private Rectangle rectangle;

    /**
     * The tint to apply to the button, if wanted.
     */
    private Color tint = null;

    /**
     * Method to call when the button is pressed.
     */
    private OnPressMethod onPress;

    /**
     * Method to call when the button is unpressed.
     */
    private OnPressMethod onUnpress;

    /**
     * Where the font will be drawn.
     */
    private Location fontLocation;

    /**
     * The font this button is rendering with.
     */
    private BitmapFont font;

    /**
     * The builder that generates fonts for this button.
     */
    private FontLoader.Builder fontBuilder;

    /**
     * The button's text.
     */
    private String text;

    /**
     * Constructs a new Button. Designed for use with the {@link Builder}.
     * @param texture Texture to display
     * @param rectangle Rectangle to display in
     * @param onPress Method to call on press
     * @param onUnpress Method to call on unpress
     * @param fontBuilder Builder to generate the font to render with
     * @param text The text to draw on the button
     */
    private Button(
        final Texture texture,
        final Rectangle rectangle,
        final OnPressMethod onPress,
        final OnPressMethod onUnpress,
        final FontLoader.Builder fontBuilder,
        final String text
    ) {
        this.texture = texture;
        this.rectangle = rectangle;
        this.onPress = onPress;
        this.onUnpress = onUnpress;
        this.fontBuilder = fontBuilder;
        this.text = text;
        visible = true;
        if (text != null) {
            updateFonts();
        }
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
     * Changes the tint to apply to this button.
     * @param tint New tint
     */
    public void setTint(final Color tint) {
        this.tint = tint;
    }

    /**
     * Checks if the button uses tint.
     * @return True if tint supplied
     */
    public boolean hasTint() {
        return tint != null;
    }

    /**
     * Gets the tint color to apply to the button.
     * @return Tint color
     */
    public Color getTint() {
        return tint;
    }

    @Override
    public void render(final SpriteBatch batch, final float delta) {
        Color oldColor = null;
        if (tint != null) {
            oldColor = new Color(batch.getColor().r, batch.getColor().g, batch.getColor().b, batch.getColor().a);
            batch.setColor(tint);
        }

        batch.draw(texture, rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
        if (text != null)
            font.draw(batch, text, fontLocation.getX(), fontLocation.getY());

        if (tint != null) {
            batch.setColor(oldColor);
        }
    }

    @Override
    public boolean onClicked() {
        if (onPress != null)
            onPress.run();

        return true;
    }
    
    @Override
    public boolean onUnclicked() {
        if (onUnpress != null)
            onUnpress.run();

        return true;
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
    public void onResize() {
        if (text != null) updateFonts();
    }

    /**
     * Updates the font used for rendering.
     */
    public void updateFonts() {
        if (fontBuilder.isScaled()) {
            font = fontBuilder.refreshScale().build();
            final Size size = calculateFontSize();
            fontLocation = rectangle.getLocation().copy();
            fontLocation.offsetX(rectangle.getWidth() / 2 + size.getWidth() / -2f);
            fontLocation.offsetY(rectangle.getHeight() / 2 + size.getHeight() / 2f);
        } else {
            font = fontBuilder.build();
        }
    }

    /**
     * Calculates the size of this font's text.
     * @return A size for this message
     */
    private Size calculateFontSize() {
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, text);

        return new FixedSize(layout.width, layout.height);
    }

    /**
     * Builds a new Button.
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
         * Size of button
         */
        private Size size;
        
        /**
         * Tint color
         */
        private Color tint;

        /**
         * On press operator method
         */
        private OnPressMethod onPress;

        /**
         * On unpress operator method
         */
        private OnPressMethod onUnpress;

        /**
         * Font generator
         */
        private FontLoader.Builder fontBuilder;
        
        /**
         * Label's text
         */
        private String text;

        /**
         * Makes a new Builder with default values.
         */
        public Builder() {
            texture = "you_forgot_to_set_the_button_texture";
            location = new FixedLocation(0, 0);
            size = new FixedSize(100, 100);
            tint = null;
            onPress = null;
            onUnpress = null;
            fontBuilder = null;
            text = null;
        }

        /**
         * Sets the texture to display with this button.
         * @param name Texture name
         * @return Builder for chaining
         */
        public Builder setTexture(final String name) {
            this.texture = name;
            return this;
        }

        /**
         * Changes the location of the button.
         * @param location Button location
         * @return Builder for chaining
         */
        public Builder setLocation(final Location location) {
            this.location = location;
            return this;
        }

        /**
         * Changes the size of the button.
         * @param size Button size
         * @return Builder for chaining
         */
        public Builder setSize(final Size size) {
            this.size = size;
            return this;
        }

        /**
         * Changes the tint of the button.
         * @param tint Button color to apply
         * @return Builder for chaining
         */
        public Builder setTint(final Color tint) {
            this.tint = tint;
            return this;
        }

        /**
         * Sets the font builder for the button.
         * @param fontBuilder Font builder
         * @return Builder for chaining
         */
        public Builder setFontBuilder(final FontLoader.Builder fontBuilder) {
            this.fontBuilder = fontBuilder;
            return this;
        }

        /**
         * Sets the text for the button.
         * @param text Text to draw
         * @return Builder for chaining
         */
        public Builder setText(final String text) {
            this.text = text;
            return this;
        }

        /**
         * Sets the action to take when the button is pressed.
         * @param onPress On press method
         * @return Builder for chaining
         */
        public Builder setOnPress(final OnPressMethod onPress) {
            this.onPress = onPress;
            return this;
        }

        /**
         * Sets the action to take when the button is unpressed.
         * @param onUnpress On unpress method
         * @return Builder for chaining
         */
        public Builder setOnUnpress(final OnPressMethod onUnpress) {
            this.onUnpress = onUnpress;
            return this;
        }

        /**
         * Builds a Button out of the parameters supplied.
         * @return New button
         */
        public Button build() {
            if (onPress == null && onUnpress == null) {
                throw new CatastrophicException("An onPress or onUnpress method must be supplied for a Button. Use an image if you don't want to do anything when clicked.");
            }

            if (text != null && fontBuilder == null) {
                throw new CatastrophicException("A font builder must be supplied for buttons with text.");
            }

            final Button button = new Button(
                KillBillGame.get().getTextureLoader().get(texture),
                new Rectangle(location, size),
                onPress,
                onUnpress,
                fontBuilder,
                text
            );

            if (tint != null) {
                button.setTint(tint);
            }

            return button;
        }
    }

    /**
     * Lambda interface for a method to call when a button is pressed.
     */
    public static interface OnPressMethod {
        /**
         * Runs on press methods.
         */
        public void run();
    }
}
