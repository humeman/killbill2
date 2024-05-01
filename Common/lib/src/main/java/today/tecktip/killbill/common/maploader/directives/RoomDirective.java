package today.tecktip.killbill.common.maploader.directives;

import java.util.ArrayList;
import java.util.List;

import today.tecktip.killbill.common.gameserver.data.TileCoordinates;
import today.tecktip.killbill.common.maploader.MapDirective;
import today.tecktip.killbill.common.maploader.MapLoader.StringPair;

/**
 * Directive for a room in a map.
 * @author cs
 */
public class RoomDirective extends MapDirective {
    private TileCoordinates location;
    private TileCoordinates size;
    private String wallTexture;
    private String floorTexture;
    private List<TileCoordinates> wallExclusions;
    private List<WallOverride> wallOverrides;
    private List<TileCoordinates> extraFloors;


    public RoomDirective(final List<StringPair> attributes) {
        super(DirectiveType.ROOM);
        location = null;
        size = null;
        wallTexture = null;
        floorTexture = null;
        wallExclusions = new ArrayList<TileCoordinates>();
        wallOverrides = new ArrayList<WallOverride>();
        extraFloors = new ArrayList<TileCoordinates>();

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
                    location = TileCoordinates.fromString(v);
                    break;
                case "size":
                    size = TileCoordinates.fromString(v);
                    break;
                case "wall":
                case "wall_texture":
                    wallTexture = v;
                    break;
                case "floor":
                case "floor_texture":
                    floorTexture = v;
                    break;
                case "exclude_wall":
                case "no_wall":
                    wallExclusions.add(TileCoordinates.fromString(v));
                    break;
                case "wall_override":
                    String[] args = v.split(",", 2);
                    if (args.length != 2) throw new IllegalArgumentException("Wall overrides must be formatted 'texture,x,y'.");
                    wallOverrides.add(
                        new WallOverride(
                            TileCoordinates.fromString(args[1]),
                            args[0]));
                    break;
                case "extra_floor":
                case "floor_override":
                    extraFloors.add(TileCoordinates.fromString(v));
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
        if (wallTexture == null) {
            throw new IllegalArgumentException("'wall_texture' cannot be null.");
        }
    }

    public TileCoordinates getLocation() {
        return location;
    }

    public TileCoordinates getSize() {
        return size;
    }

    public String getWallTexture() {
        return wallTexture;
    }

    public String getFloorTexture() {
        return floorTexture;
    }

    public List<TileCoordinates> getWallExclusions() {
        return wallExclusions;
    }

    public List<WallOverride> getWallOverrides() {
        return wallOverrides;
    }

    public List<TileCoordinates> getExtraFloors() {
        return extraFloors;
    }

    public static record WallOverride(TileCoordinates coordinates, String texture) {}
    
}
