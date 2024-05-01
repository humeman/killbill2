package today.tecktip.killbill.common.maploader.directives;

import java.util.ArrayList;
import java.util.List;

import today.tecktip.killbill.common.gameserver.data.TileCoordinates;
import today.tecktip.killbill.common.maploader.MapDirective;
import today.tecktip.killbill.common.maploader.MapLoader.StringPair;
import today.tecktip.killbill.common.maploader.ObjectFlag;

/**
 * Directive for a room in a map.
 * @author cs
 */
public class TileDirective extends MapDirective {
    private List<TileCoordinates> locations;
    private TileCoordinates size;
    private String texture;
    private List<ObjectFlag> flags;
    private int id;
    private int rotation;

    public TileDirective(final int id, final List<StringPair> attributes) {
        super(DirectiveType.ROOM);
        this.id = id;
        locations = new ArrayList<>();
        size = new TileCoordinates(1, 1);
        texture = null;
        flags = new ArrayList<>();
        rotation = 0;

        parseAttributes(attributes);
        validateAttributes();
    }

    private void parseAttributes(final List<StringPair> attributes) {
        for (final StringPair entry : attributes) {
            final String k = entry.key();
            final String v = entry.value();

            switch (k.toLowerCase()) {
                case "location":
                case "coordinates":
                case "coords":
                    locations.add(TileCoordinates.fromString(v));
                    break;
                case "size":
                    size = TileCoordinates.fromString(v);
                    break;
                case "texture":
                case "sprite":
                    texture = v;
                    break;
                case "flag":
                case "object_flag":
                    flags.add(ObjectFlag.valueOf(v.toUpperCase()));
                    break;
                case "rotation":
                    rotation = Integer.parseInt(v);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported attribute: " + k);
            }
        } 
    }

    private void validateAttributes() {
        if (locations.size() == 0) {
            throw new IllegalArgumentException("At least one 'location' directive must be specified.");
        }
        if (size == null) {
            throw new IllegalArgumentException("'size' cannot be null.");
        }
        if (texture == null) {
            throw new IllegalArgumentException("'texture' cannot be null.");
        }
    }

    public List<TileCoordinates> getLocations() {
        return locations;
    }

    public TileCoordinates getSize() {
        return size;
    }

    public String getTexture() {
        return texture;
    }

    public List<ObjectFlag> getFlags() {
        return flags;
    }

    public int getId() {
        return id;
    }

    public int getRotation() {
        return rotation;
    }
    
}
