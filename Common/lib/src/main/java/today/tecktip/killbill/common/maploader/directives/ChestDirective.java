package today.tecktip.killbill.common.maploader.directives;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.maploader.ItemType;
import today.tecktip.killbill.common.maploader.MapDirective;
import today.tecktip.killbill.common.maploader.MapLoader.StringPair;
import today.tecktip.killbill.common.maploader.ObjectFlag;

/**
 * Directive for a chest on a map.
 * @author cs
 */
public class ChestDirective extends MapDirective {
    private static final Random RANDOM = new Random();
        
    private Coordinates location;
    private Coordinates size;
    private String texture;
    private String openTexture;
    private List<ObjectFlag> flags;
    private List<LootTableObject> lootTable;
    private int overallChance;
    private int id;
    private int rotation;

    public ChestDirective(final int id, final List<StringPair> attributes) {
        super(DirectiveType.OBJECT);
        this.id = id;
        location = null;
        size = new Coordinates(1, 1);
        texture = null;
        openTexture = null;
        flags = new ArrayList<>();
        lootTable = new ArrayList<>();
        overallChance = 0;
        flags.add(ObjectFlag.INTERACTABLE);
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
                case "texture":
                case "sprite":
                    texture = v;
                    break;
                case "open_texture":
                case "open_sprite":
                    openTexture = v;
                    break;
                case "flag":
                case "object_flag": 
                    flags.add(ObjectFlag.valueOf(v.toUpperCase()));
                    break;
                case "loot_table":
                case "table":
                case "loot": 
                    LootTableObject o = LootTableObject.fromString(v);
                    overallChance += o.chance();
                    lootTable.add(o);
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
        if (texture == null) {
            throw new IllegalArgumentException("'texture' cannot be null.");
        }
        if (openTexture == null) {
            throw new IllegalArgumentException("'open_texture' cannot be null.");
        }
        if (lootTable.size() == 0) {
            throw new IllegalArgumentException("Must provide at least one 'loot_table' entry.");
        }
    }

    public Coordinates getLocation() {
        return location;
    }

    public Coordinates getSize() {
        return size;
    }

    public String getTexture() {
        return texture;
    }

    public String getOpenTexture() {
        return openTexture;
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

    public List<LootTableObject> getLootTable() {
        return lootTable;
    }

    public int getOverallChance() {
        return overallChance;
    }

    public ItemType rollLootTable() {
        return rollLootTable(RANDOM);
    }

    public ItemType rollLootTable(final Random random) {
        // Get a random number up to overallChance
        int rand = random.nextInt(overallChance);

        int sum = 0;
        for (final LootTableObject o : lootTable) {
            sum += o.chance;
            if (rand < sum) return o.type();
        }

        throw new RuntimeException("This should never happen.");
    }

    public static record LootTableObject(int chance, ItemType type) {
        public static LootTableObject fromString(final String value) {
            // Split on comma
            String[] values = value.split(",", 2);

            if (values.length != 2) {
                throw new IllegalArgumentException("Invalid loot table entry: Must be formatted '_chance_,_ITEM_'");
            }

            int chance;
            try {
                chance = Integer.valueOf(values[0]);
            } catch (final Exception e) {
                throw new IllegalArgumentException("Invalid loot table entry: Couldn't parse chance as an integer");
            }

            ItemType type;
            try {
                type = ItemType.valueOf(values[1].toUpperCase());
            } catch (final Exception e) {
                throw new IllegalArgumentException("Invalid item type: " + values[1]);
            }

            return new LootTableObject(chance, type);
        }
    }
    
}
