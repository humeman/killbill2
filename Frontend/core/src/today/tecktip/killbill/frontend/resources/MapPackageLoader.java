package today.tecktip.killbill.frontend.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import today.tecktip.killbill.common.gameserver.games.GameConfig;
import today.tecktip.killbill.common.maploader.KillBillMap;
import today.tecktip.killbill.common.maploader.MapLoader;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;

public class MapPackageLoader {
    /**
     * The directory to load maps from.
     */
    private static final String MAP_DIRECTORY = "maps";

    /**
     * The file type for map files.
     */
    private static final String MAP_FILE_TYPE = ".kbmap";

    /**
     * The file type for map configs.
     */
    private static final String CONFIG_FILE_TYPE = ".kbconfig.json";

    /**
     * Internal map of all textures registered.
     */
    private List<MapPackage> maps;

    /**
     * Creates a new MapPackageLoader. No maps are loaded until {@link #load} is called.
     */
    public MapPackageLoader() {
        maps = new ArrayList<>();
    }

    /**
     * Loads in all maps from {@link #MAP_DIRECTORY}.
     */
    public void load() throws IOException {
        if (maps.size() > 0) maps.clear();
        load(MAP_DIRECTORY);
    }

    /**
     * Loads in all maps.
     * @see #load()
     * @param sourceDir Directory to load from
     */
    private void load(final String sourceDir) throws IOException {
        for (final FileHandle directory : Gdx.files.internal(sourceDir).list()) {
            if (directory.isDirectory()) {
                GameConfig config = null;
                List<InputStream> mapStreams = new ArrayList<>();
                // List the insides :)
                for (final FileHandle file : directory.list()) {
                    if (file.name().endsWith(MAP_FILE_TYPE)) {
                        // Load the map
                        try {
                            mapStreams.add(file.read());
                        } catch (final Throwable t) {
                            throw new CatastrophicException("Invalid map present in " + directory.name() + ": " + t.getMessage(), t);
                        }
                    } else if (file.name().endsWith(CONFIG_FILE_TYPE)) {
                        // Load the config
                        try {
                            if (config != null) {
                                throw new CatastrophicException("Duplicate config present in '" + directory.name() + "'. Please remove one.");
                            }

                            config = GameConfig.fromJson(file.readString());
                        } catch (final Throwable t) {
                            throw new CatastrophicException("Invalid config present in " + directory.name() + ": " + t.getMessage(), t);
                        }
                    } else {
                        throw new CatastrophicException("Invalid file present in map directory '" + directory.name() + "'' (invalid extension). Please remove it: " + file);
                    }
                }

                if (mapStreams.size() == 0) {
                    throw new CatastrophicException("Invalid map in directory " + directory.name() + ": Missing '" + MAP_FILE_TYPE + "' file.");
                }

                if (config == null) {
                    throw new CatastrophicException("Invalid map in directory " + directory.name() + ": Missing '" + CONFIG_FILE_TYPE + "' file.");
                }

                final KillBillMap map = MapLoader.load(mapStreams);
                maps.add(new MapPackage(map, config));

                // And log its creation
                Gdx.app.log(MapPackageLoader.class.getSimpleName(), "Registered map: " + map.getDisplayName());
            } else {
                throw new CatastrophicException("Invalid file present in map directory (not a folder). Please remove it: " + directory);
            }
        }
    }
    
    /**
     * Retrieves a map from the map loader.
     * @param index The index of the map.
     * @return The map package at this index.
     */
    public MapPackage get(final int index) {
        return maps.get(index);
    }
    
    /**
     * Gets a list of all maps registered. Don't edit this.
     * @return The list of all loaded maps.
     */
    public List<MapPackage> getAll() {
        return maps;
    }

    /**
     * A map package, including the map itself and its game config.
     */
    public static record MapPackage(KillBillMap map, GameConfig config) {} 
}
