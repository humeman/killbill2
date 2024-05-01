package today.tecktip.killbill.frontend.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;

/**
 * Dynamically loads textures from the assets directory.
 * @author cs
 */
public class FontLoader {
    /**
     * The directory to load fonts from.
     */
    private static final String FONT_DIRECTORY = "fonts";

    /**
     * The default font's name. Returned when a font was not found.
     */
    private static final String DEFAULT_FONT = "error";

    /**
     * The types of files that will be loaded by the font loader.
     */
    private static final String[] ALLOWABLE_FILE_TYPES = new String[] {
        "ttf"
    };

    /**
     * Valid path characters.
     */
    private static final Pattern VALID_NORMALIZED_CHARACTERS = Pattern.compile("^[a-z0-9_]*$");

    /**
     * Internal map of all fonts registered.
     */
    private Map<String, FreeTypeFontGenerator> fonts;

    /**
     * Missing fonts that have been logged already. Prevents spam.
     */
    private List<String> loggedMissing;

    /**
     * Creates a new FontLoader. No fonts are loaded until {@link #load} is called.
     */
    public FontLoader() {
        fonts = new HashMap<>();
        loggedMissing = new ArrayList<>();
    }

    /**
     * Loads in all textures from {@link #FONT_DIRECTORY}.
     * <p>
     * The default name format is the full lowercase path separated by underscores, and
     *  with no file extension.
     * <p>
     * For example:
     * <ul>
     *  <li><code>assets/fonts/main.ttf</code> -> <code>main</code></li>
     *  <li><code>assets/fonts/extra/something.ttf</code> -> <code>extra_something</code></li>
     */
    public void load() throws IOException {
        load(FONT_DIRECTORY);

        if (!fonts.containsKey(DEFAULT_FONT)) {
            throw new CatastrophicException("Missing default font: " + DEFAULT_FONT);
        }
    }

    /**
     * Loads in all fonts.
     * @see #load()
     * @param directory Directory to load from
     */
    private void load(final String directory) throws IOException {
        for (final FileHandle file : Gdx.files.internal(directory).list()) {
            if (!file.isDirectory()) {
                final int indexOfDot = file.name().lastIndexOf('.');
                if (indexOfDot == -1) {
                    throw new CatastrophicException("Invalid file present in font directory (missing filetype extension). Please remove it: " + file);
                }
                final String fileType = file.name().substring(indexOfDot + 1).toLowerCase();

                // Check if this a valid font type
                boolean valid = false;
                for (final String allowableExtension : ALLOWABLE_FILE_TYPES) {
                    if (fileType.equals(allowableExtension)) {
                        valid = true;
                        break;
                    }
                }

                if (!valid) {
                    throw new CatastrophicException("Invalid file present in font directory (unsupported file type: " + fileType + "). Please remove it: " + file);
                }

                // Normalize the file name
                final List<String> normalizedNameParts = new ArrayList<>();
                for (final String pathSection : file.toString().replace('\\', '/').split("/")) {
                    if (pathSection.equals(FONT_DIRECTORY)) continue;
                    else normalizedNameParts.add(pathSection.toLowerCase());
                }

                // The last item in this path contains a filename. Filter it out using the dot index we got earlier.
                String lastPart = normalizedNameParts.remove(normalizedNameParts.size() - 1);
                lastPart = lastPart.substring(0, indexOfDot);
                normalizedNameParts.add(lastPart);

                // Combine into a normalized path
                final String normalizedName = normalizedNameParts.stream()
                    .collect(Collectors.joining("_"));

                // Assert that this name is a valid normalized name
                final Matcher matcher = VALID_NORMALIZED_CHARACTERS.matcher(normalizedName);
                
                if (!matcher.matches()) {
                    throw new CatastrophicException(
                        "Invalid file present in font directory (normalized name does not meet constraints). Please rename it to contain " +
                        "only lowercase characters, numbers, and underscores: " + normalizedName
                    );
                }
                
                // Make sure this isn't a duplicate (possible with different file types)
                if (fonts.containsKey(normalizedName)) {
                    throw new CatastrophicException("Invalid file present in font directory (collision between two files). Please rename one of them: " + normalizedName);
                }

                // Now, make a texture for it
                fonts.put(normalizedName, new FreeTypeFontGenerator(file));

                // And log its creation
                Gdx.app.log(FontLoader.class.getSimpleName(), "Registered font: " + normalizedName);
            }
            else {
                load(file.path());
            }
        }
    }
    
    /**
     * Creates a builder for a font from the font loader with default parameters. If it is not found, the default font is used.
     * @param name The name of the font to retrieve (see {@link #load} for naming conventions)
     * @return A builder for this font with default parameters applied
     */
    public Builder newBuilder(final String name) {
        FreeTypeFontGenerator font = fonts.get(name);

        if (font == null) {
            if (!loggedMissing.contains(name)) {
                Gdx.app.error(FontLoader.class.getSimpleName(), "Missing font: " + name);
                loggedMissing.add(name);
            }
            font = fonts.get(DEFAULT_FONT);
        }

        return new Builder(font);
    }

    /**
     * Constructs fonts based on a generator, allowing easier parameter customization.
     */
    public static class Builder {
        /**
         * The parameters to apply to the font
         */
        private FreeTypeFontParameter parameters;

        /**
         * The font generator
         */
        private FreeTypeFontGenerator generator;

        /**
         * The current font scale (for scaled sizes)
         */
        private int scaledSize;

        /**
         * The current border width (for scaled sizes)
         */
        private int borderWidth;

        /**
         * True if the shadow is scaled.
         */
        private boolean shadowScaled;

        /**
         * The current shadow X offset (for scaled sizes)
         */
        private int shadowX;

        /**
         * The current shadow Y offset (for scaled sizes)
         */
        private int shadowY;

        /**
         * Last font that has been generated with this builder.
         */
        private BitmapFont latestFont;

        /**
         * Whether the font has changed since last generation.
         */
        private boolean changed;

        /**
         * Constructs a new font builder. A new Builder must be used for each font type.
         * @param generator Font generator instance
         */
        public Builder(final FreeTypeFontGenerator generator) {
            this.generator = generator;
            scaledSize = -1;
            borderWidth = -1;
            shadowScaled = false;
            shadowX = -1;
            shadowY = -1;
            parameters = new FreeTypeFontParameter();
            parameters.size = 16;
            parameters.color = new Color(0, 0, 0, 1);
            latestFont = null;
            changed = true;
        }

        /**
         * Sets the font size to a fixed pixel size. Prefer {@link #setScaledSize} where possible.
         * @param size Size (in pixels)
         * @return Builder for chaining
         */
        public Builder setSize(final int size) {
            parameters.size = size;
            this.scaledSize = -1;
            changed = true;
            return this;
        }

        /**
         * Sets the font size to a scaled pixel size.
         * <p>
         * Calling this method, you specify a base font size. The size is then
         *  multiplied by the width of the screen divided by 1000. So, for a screen
         *  that is 500px wide, you'll get a font half as big, and for a screen 2000px
         *  wide, a font that is 2x as large will be returned.
         * @param size Size (factored)
         * @return Builder for chaining
         */
        public Builder setScaledSize(final int size) {
            int newSize = (int) ((KillBillGame.get().getWidth() / 1000f) * size);
            if (newSize == parameters.size) {
                // Nothing changed
                return this;
            }
            parameters.size = newSize;
            this.scaledSize = size;

            if (borderWidth > 0) {
                setBorder(parameters.borderColor, borderWidth);
            }

            if (shadowScaled) {
                setShadow(parameters.shadowColor, shadowX, shadowY);
            }

            changed = true;
            return this;
        }

        /**
         * Refreshes the font scale in the event of window resizes.
         * @return Builder for chaining
         */
        public Builder refreshScale() {
            if (scaledSize < 0) throw new CatastrophicException("A scaled font size was not used here.");

            setScaledSize(scaledSize);
            if (borderWidth > 0) {
                setBorder(parameters.borderColor, borderWidth);
            }

            if (shadowScaled) {
                setShadow(parameters.shadowColor, shadowX, shadowY);
            }

            return this;
        }

        /**
         * Sets the color of the font.
         * @param color New color
         * @return Builder for chaining
         */
        public Builder setColor(final Color color) {
            parameters.color = color;
            changed = true;
            return this;
        }

        /**
         * Adds a border to the font. Width will be scaled only if the font size is scaled.
         * @param color Border color
         * @param width Size (in pixels) of the border
         * @return Builder for chaining
         */
        public Builder setBorder(final Color color, final int width) {
            int newWidth = (int) (isScaled() ? ((KillBillGame.get().getWidth() / 1000f) * width) : width);
            if (parameters.borderColor.equals(color) && newWidth == parameters.borderWidth) {
                return this; // No change
            }
            parameters.borderWidth = newWidth;
            parameters.borderColor = color;
            if (isScaled()) {
                borderWidth = width;
            }
            changed = true;
            return this;
        }

        /**
         * Changes the font drop shadow.
         * @param color Color of the shadow
         * @param x X location
         * @param y Y location
         * @return Builder for chaining
         */
        public Builder setShadow(final Color color, final int x, final int y) {
            int newShadowOffsetX = (int) (isScaled() ? ((KillBillGame.get().getWidth() / 1000f) * x) : x);
            int newShadowOffsetY = (int) (isScaled() ? ((KillBillGame.get().getWidth() / 1000f) * y) : y);
            if (newShadowOffsetX == parameters.shadowOffsetX && newShadowOffsetY == parameters.shadowOffsetY && parameters.shadowColor.equals(color)) {
                return this; // Nothing changed
            }
            parameters.shadowOffsetX = newShadowOffsetX;
            parameters.shadowOffsetY = newShadowOffsetY;
            parameters.shadowColor = color;
            if (isScaled()) {
                shadowX = x;
                shadowY = y;
                shadowScaled = true;
            }
            changed = true;
            return this;
        }

        /**
         * Checks if the font is using a scaled size.
         * @return True if scaled
         */
        public boolean isScaled() {
            return scaledSize >= 0;
        }

        /**
         * Gets the current FreeType font parameters used.
         * @return Parameters
         */
        public FreeTypeFontParameter getCurrentParameters() {
            return parameters;
        }

        /**
         * Builds the current parameters into a font. Fonts are cached until any
         *  parameter changes, requiring regeneration -- so you can share this
         *  with other elements. Just don't change it after one has already
         *  built its font or it will be disposed of (leading to black boxes)!
         * @return Generated font
         */
        public BitmapFont build() {
            if (!changed) {
                // Identical to last font
                return latestFont;
            }
            if (latestFont != null) {
                latestFont.dispose();
            }
            latestFont = generator.generateFont(parameters);
            changed = false;
            return latestFont;
        }
    }
}
