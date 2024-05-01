package today.tecktip.killbill.frontend.ui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.KillBillGame.Platform;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.resources.FontLoader;
import today.tecktip.killbill.frontend.ui.Location;
import today.tecktip.killbill.frontend.ui.Rectangle;
import today.tecktip.killbill.frontend.ui.Size;
import today.tecktip.killbill.frontend.ui.UiElement;
import today.tecktip.killbill.frontend.ui.UiRenderer;
import today.tecktip.killbill.frontend.ui.Location.FixedLocation;
import today.tecktip.killbill.frontend.ui.Size.FixedSize;

/**
 * Represents a text input box on the UI.
 * @author cs
 */
public class TextInput implements UiElement {
    
    /**
     * Constructs a new builder for a text input.
     * @return New TextInput builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Visibility state
     */
    private boolean visible;

    /**
     * Texture this TextInput is drawing.
     */
    private Texture texture;

    /**
     * Rectangle where the box is drawn.
     */
    private Rectangle rectangle;

    /**
     * Where the font will be drawn.
     */
    private Location fontLocation;

    /**
     * The font this textinput is rendering with.
     */
    private BitmapFont font;

    /**
     * The builder that generates fonts for this textinput.
     */
    private FontLoader.Builder fontBuilder;

    /**
     * The textinput's placeholder value.
     */
    private String placeholder;

    /**
     * The textinput's value.
     */
    private String text;

    /**
     * If true, line breaks will be accepted and rendered
     */
    private boolean allowLineBreaks;

    /**
     * Whether the text box is selected or not.
     */
    private boolean selected;

    /**
     * True if the cursor character is currently drawn.
     */
    private boolean cursorDrawn;

    /**
     * The time after which the cursor will swap.
     */
    private float swapTimer;

    /**
     * Character where text should begin to be drawn (in case it goes off the screen)
     */
    private int startDrawChar;

    /**
     * If true, the message's content is obscured
     */
    private boolean secret;


    /**
     * text box to switch to if read "tab" input
     */
    private TextInput nextTextBox;

    /**
     * button that submits this text box
     */
    private Button submitButton;

    /**
     * Constructs a new TextInput. Designed for use with the {@link Builder}.
     * @param texture Texture to display
     * @param rectangle Rectangle to display in
     * @param fontBuilder Builder to generate the font to render with
     * @param placeholder The placeholder text to draw on the input
     * @param text The default text to fill in
     * @param allowLineBreaks If true, line breaks will be accepted and rendered
     */
    private TextInput(
        final Texture texture,
        final Rectangle rectangle,
        final FontLoader.Builder fontBuilder,
        final String placeholder,
        final String text,
        final boolean allowLineBreaks,
        final boolean secret,
        final TextInput nextTextBox,
        final Button submitButton
    ) {
        this.texture = texture;
        this.rectangle = rectangle;
        this.fontBuilder = fontBuilder;
        this.placeholder = placeholder;
        this.text = text;
        this.allowLineBreaks = allowLineBreaks;
        this.secret = secret;
        this.nextTextBox = nextTextBox;
        selected = false;
        startDrawChar = 0;
        visible = true;
        updateFonts();
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
     * Changes the tint of this label.
     * @param tint New tint
     */
    public void setTint(final Color tint) {
        font.setColor(tint);
    }

    /**
     * Gets the tint color to apply to the input.
     * @return Tint color
     */
    public Color getTint() {
        return font.getColor();
    }

    @Override
    public void render(final SpriteBatch batch, final float delta) {
        batch.draw(texture, rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
        String realText = text;
        if (text.length() > 0 || selected) {
            if (secret) {
                realText = new String(new char[text.length()]).replaceAll("\0", "*");
            }

            if (selected) {
                font.draw(batch, realText.substring(startDrawChar) + (cursorDrawn ? "|" : ""), fontLocation.getX(), fontLocation.getY());
                swapTimer -= delta;
                if (swapTimer < 0) {
                    swapTimer = 1f;
                    cursorDrawn = !cursorDrawn;
                }
            } else {
                font.draw(batch, realText.substring(startDrawChar), fontLocation.getX(), fontLocation.getY());
            }
            
        } else if (placeholder != null) {
            // Store the old font color
            float oldA = font.getColor().a;
            font.setColor(font.getColor().r, font.getColor().g, font.getColor().b, font.getColor().a * 0.5f);

            font.draw(batch, placeholder.substring(startDrawChar), fontLocation.getX(), fontLocation.getY());

            font.setColor(font.getColor().r, font.getColor().g, font.getColor().b, oldA);
        }
    }

    /**
     * Selects this text input box.
     */
    public void select() {
        selected = true;
        cursorDrawn = true;
        swapTimer = 1f;

        // Deselect all other TextInputs
        final UiRenderer activeRenderer = KillBillGame.get().getCurrentScreen().getUiRenderer();
        if (activeRenderer != null) {
            activeRenderer.forEachElement(
                element -> {
                    if (element instanceof TextInput && element != this) {
                        final TextInput inp = (TextInput) element;

                        inp.unselect();
                    }

                    return false;
                }
            );
        } 
        
        if (KillBillGame.get().getPlatform().equals(Platform.ANDROID)) {
            Gdx.input.setOnscreenKeyboardVisible(selected);
        }
    }

    /**
     * Unselects this text input box.
     */
    public void unselect() {
        selected = false;
    }

    @Override
    public boolean onClicked() {
        if (selected) unselect();
        else select();

        return true;
    }

    @Override
    public boolean onUnclicked() {
        // Do nothing
        return false;
    }

    @Override
    public boolean onKeyPressed(final int key) {
        return false;
    }

    @Override
    public boolean onKeyTyped(final char c) {
        if (selected) {
            if (c == '\b') {
                if (text.length() == 0) return true;
                text = text.substring(0, text.length() - 1);
                refreshSizing();
                return true;
            }
            if (c == '\n' && !allowLineBreaks) {
                selected = false;
                if (KillBillGame.get().getPlatform().equals(Platform.ANDROID)) {
                    Gdx.input.setOnscreenKeyboardVisible(false);
                }

                if (submitButton == null) {
                    return true;
                }
                submitButton.onClicked();
                return true;
            }
            if (c == '\t') {
                if (nextTextBox == null) {
                    return true;
                }
                nextTextBox.select();
                return true;
            }
            text += c;
            refreshSizing();
            return true;
        }
        return false;
    }

    @Override
    public Rectangle getRectangle() {
        return rectangle;
    }

    @Override
    public void onResize() {
        updateFonts();
    }

    /**
     * Gets the text the user has typed.
     */
    public String getText() {
        return text;
    }

    /**
     * Changes the text stored in the input box.
     * @param text New text
     */
    public void setText(final String text) {
        this.text = text;
        refreshSizing();
    }

    /**
     * Updates this input's fonts.
     */
    public void updateFonts() {
        if (fontBuilder.isScaled()) {
            font = fontBuilder.refreshScale().build();
        } else {
            font = fontBuilder.build();
        }

        Color oldColor = fontBuilder.getCurrentParameters().color;
        fontBuilder.setColor(oldColor);

        refreshSizing();
    }

    /**
     * Calculates the size of this font's text.
     * @return A size for this message
     */
    private Size calculateFontSize() {
        GlyphLayout layout = new GlyphLayout();
        if (text.length() == 0)
            layout.setText(font, placeholder.substring(startDrawChar));
        else if (placeholder != null) layout.setText(font, text.substring(startDrawChar));
        else return new FixedSize(0, 0);

        return new FixedSize(layout.width, layout.height);
    }

    /**
     * Refreshes the font sizing.
     */
    private void refreshSizing() {
        Size size = calculateFontSize();

        startDrawChar = 0;
        while (size.getWidth() > rectangle.getWidth()) {
            startDrawChar++;
            size = calculateFontSize();
        }

        fontLocation = rectangle.getLocation().copy();
        fontLocation.offsetX(font.getXHeight());
        fontLocation.offsetY(rectangle.getHeight() / 2 + size.getHeight() / 2f);
    }

    public void setNext(TextInput next) {
        this.nextTextBox = next;
    }

    public void setSubmit(Button submit) {
        this.submitButton = submit;
    }

    /**
     * Builds a new TextInput.
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
         * Size of input box
         */
        private Size size;
        
        /**
         * Tint color
         */
        private Color tint;

        /**
         * Font generator
         */
        private FontLoader.Builder fontBuilder;
        
        /**
         * Label's placeholder text
         */
        private String placeholder;

        /**
         * Label's starting text
         */
        private String text;

        /**
         * If true, line breaks will be accepted and rendered
         */
        private boolean allowLineBreaks;

        /**
         * If true, the box's input is obscured
         */
        private boolean secret;

        /**
         * text box to switch to if read "tab" input
         */
        private TextInput nextTextBox;

        /**
         * Button to submit this form
         */
        private Button submitButton;

        /**
         * Makes a new Builder with default values.
         */
        public Builder() {
            texture = "you_forgot_to_set_the_input_texture";
            location = new FixedLocation(0, 0);
            size = new FixedSize(100, 100);
            tint = null;
            fontBuilder = null;
            placeholder = "add a placeholder >:(";
            text = "";
            allowLineBreaks = false;
            secret = false;
            nextTextBox = null;
            submitButton = null;
        }

        /**
         * Sets the texture to display with this input.
         * @param name Texture name
         * @return Builder for chaining
         */
        public Builder setTexture(final String name) {
            this.texture = name;
            return this;
        }

        /**
         * Changes the location of the input.
         * @param location Input location
         * @return Builder for chaining
         */
        public Builder setLocation(final Location location) {
            this.location = location;
            return this;
        }

        /**
         * Changes the size of the input.
         * @param size Input size
         * @return Builder for chaining
         */
        public Builder setSize(final Size size) {
            this.size = size;
            return this;
        }

        /**
         * Changes the tint of the input.
         * @param tint Input color to apply
         * @return Builder for chaining
         */
        public Builder setTint(final Color tint) {
            this.tint = tint;
            return this;
        }

        /**
         * Sets the font builder for the input.
         * @param fontBuilder Font builder
         * @return Builder for chaining
         */
        public Builder setFontBuilder(final FontLoader.Builder fontBuilder) {
            this.fontBuilder = fontBuilder;
            return this;
        }

        /**
         * Sets the starting text for the input.
         * @param text Text to draw
         * @return Builder for chaining
         */
        public Builder setText(final String text) {
            this.text = text;
            return this;
        }

        /**
         * Sets the placeholder text for the input.
         * @param placeholder Text to draw with no input set
         * @return Builder for chaining
         */
        public Builder setPlaceholder(final String text) {
            this.placeholder = text;
            return this;
        }

        /**
         * Determines if line breaks will be accepted.
         * @param allowLineBreaks If true, line breaks will be accepted and rendered
         * @return Builder for chaining
         */
        public Builder setAllowLineBreaks(final boolean allowLineBreaks) {
            this.allowLineBreaks = allowLineBreaks;
            return this;
        }

        /**
         * Makes the content of this input box secret.
         * @param secret If true, input is obscured
         * @return Builder for chaining
         */
        public Builder setSecret(final boolean secret) {
            this.secret = secret;
            return this;
        }

        /**
         * Builds a TextInput out of the parameters supplied.
         * @return New text input
         */
        public TextInput build() {
            if (fontBuilder == null) {
                throw new CatastrophicException("A font builder must be supplied.");
            }

            final TextInput textInput = new TextInput(
                KillBillGame.get().getTextureLoader().get(texture),
                new Rectangle(location, size),
                fontBuilder,
                placeholder,
                text,
                allowLineBreaks,
                secret,
                nextTextBox,
                submitButton
            );

            if (tint != null) {
                textInput.setTint(tint);
            }

            return textInput;
        }

        /**
         * sets the next text box to be selected if "tab" is pressed
         * @param next next box to be selected
         * @return Builder for chaining
         */
        public Builder setNext(final TextInput next) {
            nextTextBox = next;
            return this;
        }

        /**
         * sets the button to submit this form
         * @param submit button that submits this form
         * @return Builder for chaining
         */
        public Builder setSubmit(final Button submit) {
            submitButton = submit;
            return this;
        }
    }
}
