package today.tecktip.killbill.common.maploader.directives;

import java.util.ArrayList;
import java.util.List;

import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.maploader.MapDirective;
import today.tecktip.killbill.common.maploader.MapLoader.StringPair;
import today.tecktip.killbill.common.maploader.ObjectFlag;

/**
 * Directive for an object on a map.
 * @author cs
 */
public class ObjectDirective extends MapDirective {
    private List<Coordinates> locations;
    private Coordinates size;
    private String texture;
    private List<ObjectFlag> flags;
    private int id;
    private int rotation;

    public ObjectDirective(final int id, final List<StringPair> attributes) {
        super(DirectiveType.OBJECT);
        this.id = id;
        locations = new ArrayList<>();
        size = new Coordinates(1, 1);
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
                    locations.add(Coordinates.fromString(v));
                    break;
                case "size":
                    size = Coordinates.fromString(v);
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

    public List<Coordinates> getLocations() {
        return locations;
    }

    public Coordinates getSize() {
        return size;
    }

    public String getTexture() {
        return texture;
    }

    public int getId() {
        return id;
    }

    public List<ObjectFlag> getFlags() {
        return flags;
    }

    public int getRotation() {
        return rotation;
    }
    
}
