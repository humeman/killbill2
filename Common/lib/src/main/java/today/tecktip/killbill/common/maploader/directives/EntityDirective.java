package today.tecktip.killbill.common.maploader.directives;

import java.util.List;

import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.maploader.MapDirective;
import today.tecktip.killbill.common.maploader.MapLoader.StringPair;

/**
 * Directive for an entity in a map.
 * @author cs
 */
public class EntityDirective extends MapDirective {
    private EntityType type;
    private Coordinates location;
    private Coordinates size;
    private String heldItemTexture;
    private int id;
    private int rotation;
    private String texturePrefix;

    public EntityDirective(final int id, final List<StringPair> attributes) {
        super(DirectiveType.ENTITY);
        location = null;
        size = new Coordinates(1, 1);
        heldItemTexture = null;
        this.id = id;
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
                    if (location != null) throw new IllegalArgumentException("'location' is not repeatable.");
                    location = Coordinates.fromString(v);
                    break;
                case "size":
                    size = Coordinates.fromString(v);
                    break;
                case "type":
                    type = EntityType.valueOf(v.toUpperCase());
                    break;
                case "held_item_texture":
                case "held_item":
                case "holding":
                case "item":
                    heldItemTexture = v;
                    break;
                case "texture":
                case "texture_prefix":
                    texturePrefix = v;
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
        if (location == null) {
            throw new IllegalArgumentException("'location' cannot be null.");
        }
        if (size == null) {
            throw new IllegalArgumentException("'size' cannot be null.");
        }
        if (type == null) {
            throw new IllegalArgumentException("'type' cannot be null.");
        }
    }

    public Coordinates getLocation() {
        return location;
    }

    public Coordinates getSize() {
        return size;
    }

    public String getHeldItemTexture() {
        return heldItemTexture;
    }

    public EntityType getEntityType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public int getRotation() {
        return rotation;
    }

    public String getTexturePrefix() {
        return texturePrefix;
    }


    public static enum EntityType {
        EMPLOYEE,
        CLAYMORE_ROOMBA
    }
    
}
