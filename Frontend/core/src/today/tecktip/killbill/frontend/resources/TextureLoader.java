package today.tecktip.killbill.frontend.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

import today.tecktip.killbill.frontend.exceptions.CatastrophicException;

/**
 * Dynamically loads textures from the assets directory.
 * @author cs
 */
public class TextureLoader {
    /**
     * The directory to load textures from.
     */
    private static final String TEXTURE_DIRECTORY = "textures";

    /**
     * The default texture's name. Returned when a texture was not found.
     */
    private static final String DEFAULT_TEXTURE = "error";

    /**
     * The types of files that will be loaded by the texture loader.
     */
    private static final String[] ALLOWABLE_FILE_TYPES = new String[] {
        "png",
        "jpg",
        "jpeg"
    };

    /**
     * Valid path characters.
     */
    private static final Pattern VALID_NORMALIZED_CHARACTERS = Pattern.compile("^[a-z0-9_]*$");

    /**
     * Random texture picker.
     */
    private static final Random RANDOM = new Random();

    /**
     * Internal map of all textures registered.
     */
    private Map<String, Texture> textures;

    /**
     * Missing textures that have been logged already. Prevents spam.
     */
    private List<String> loggedMissing;

    /**
     * Creates a new TextureLoader. No textures are loaded until {@link #load} is called.
     */
    public TextureLoader() {
        textures = new HashMap<>();
        loggedMissing = new ArrayList<>();
    }

    /**
     * Loads in all textures from {@link #TEXTURE_DIRECTORY}.
     * <p>
     * The default name format is the full lowercase path separated by underscores, and
     *  with no file extension.
     * <p>
     * For example:
     * <ul>
     *  <li><code>assets/images/menu/button.png</code> -> <code>menu_button</code></li>
     *  <li><code>assets/images/game/Players/PLaYeR_1.png</code> -> <code>game_players_player_1</code></li>
     */
    public void load() throws IOException {
        if (textures.size() != 0) textures.clear();

        load(TEXTURE_DIRECTORY);

        if (!textures.containsKey(DEFAULT_TEXTURE)) {
            throw new CatastrophicException("Missing default texture: " + DEFAULT_TEXTURE);
        }
    }

    /**
     * Loads in all textures.
     * @see #load()
     * @param directory Directory to load from
     */
    private void load(final String directory) throws IOException {
        for (final FileHandle fileHandle : Gdx.files.internal(directory).list()) {
            if (!fileHandle.isDirectory()) {
                final int indexOfDot = fileHandle.name().lastIndexOf('.');
                if (indexOfDot == -1) {
                    throw new CatastrophicException("Invalid file present in texture directory (missing filetype extension). Please remove it: " + fileHandle);
                }
                final String fileType = fileHandle.name().substring(indexOfDot + 1).toLowerCase();

                // Check if this is an image texture
                boolean valid = false;
                for (final String allowableExtension : ALLOWABLE_FILE_TYPES) {
                    if (fileType.equals(allowableExtension)) {
                        valid = true;
                        break;
                    }
                }

                if (!valid) {
                    throw new CatastrophicException("Invalid file present in texture directory (unsupported file type: " + fileType + "). Please remove it: " + fileHandle);
                }

                // Normalize the file name
                final List<String> normalizedNameParts = new ArrayList<>();
                for (final String pathSection : fileHandle.path().replace('\\', '/').split("/")) {
                    if (pathSection.equals(TEXTURE_DIRECTORY)) continue;
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
                        "Invalid file present in texture directory (normalized name does not meet constraints). Please rename it to contain " +
                        "only lowercase characters, numbers, and underscores: " + normalizedName
                    );
                }
                
                // Make sure this isn't a duplicate (possible with different file types)
                if (textures.containsKey(normalizedName)) {
                    throw new CatastrophicException("Invalid file present in texture directory (collision between two files). Please rename one of them: " + normalizedName);
                }

                // Now, make a texture for it
                textures.put(normalizedName, new Texture(fileHandle));

                // And log its creation
                Gdx.app.log(TextureLoader.class.getSimpleName(), "Registered texture: " + normalizedName);
            }
            else {
                load(fileHandle.path());
            }
        }
    }
    
    /**
     * Retrieves a texture from the texture loader. If it is not found, the default texture is used.
     * @param name The name of the texture to retrieve (see {@link #load} for naming conventions)
     * @return The texture matching this name
     */
    public Texture get(final String name) {
        // If the texture contains a wildcard at the end, we can do some magic
        if (name.endsWith("*")) {
            // Find all textures starting with the prefix
            String prefix = name.substring(0, name.length() - 1);
            List<Texture> possibleTextures = new ArrayList<>();

            for (final Map.Entry<String, Texture> kv : textures.entrySet()) {
                if (kv.getKey().startsWith(prefix)) {
                    possibleTextures.add(kv.getValue());
                }
            }

            if (possibleTextures.size() == 0) return textures.get(DEFAULT_TEXTURE);

            return possibleTextures.get(RANDOM.nextInt(possibleTextures.size()));
        }

        final Texture texture = textures.get(name);

        if (texture == null) {
            if (!loggedMissing.contains(name)) {
                Gdx.app.error(TextureLoader.class.getSimpleName(), "Missing texture: " + name);
                loggedMissing.add(name);
            }
            return textures.get(DEFAULT_TEXTURE);
        }

        return texture;
    }
}
