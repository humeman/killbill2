package today.tecktip.killbill.common.maploader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import today.tecktip.killbill.common.maploader.directives.ChestDirective;
import today.tecktip.killbill.common.maploader.directives.ConfigDirective;
import today.tecktip.killbill.common.maploader.directives.EntityDirective;
import today.tecktip.killbill.common.maploader.directives.ObjectDirective;
import today.tecktip.killbill.common.maploader.directives.RoomDirective;
import today.tecktip.killbill.common.maploader.directives.TileDirective;

public class MapLoader {
    private static int entityId;
    private static int chestId;
    private static int tileId;
    private static int objectId;
    private static boolean hasConfig;

    private static AtomicBoolean lock;
    static {
        lock = new AtomicBoolean(false);
    }

    public static KillBillMap load(final List<InputStream> files) {
        if (!lock.compareAndSet(false, true)) {
            throw new IllegalStateException("Attempted to load a map while another load was active.");
        }

        try {
            // Create an empty map
            final KillBillMap map = new KillBillMap();
            final List<String> mapStr = new ArrayList<>();
            
            entityId = 0;
            chestId = 0;
            tileId = 0;
            objectId = 0;
            hasConfig = false;

            for (final InputStream stream : files) {
                load(map, stream, mapStr);
            }

            if (!hasConfig) {
                throw new IllegalArgumentException("Exactly one 'config' directive must be specified.");
            }

            map.setString(String.join("\n", mapStr));
            return map;
        } finally {
            lock.set(false);
        }
    }

    private static void load(final KillBillMap map, final InputStream fileStream, final List<String> mapStr) {
        // Read out the file, line by line
        String line = "";
        String currentDirective = null;
        List<StringPair> attributes = new ArrayList<>();
        int lineNum = 1;
        int directiveStart = -1;
        try (final Scanner scanner = new Scanner(fileStream)) {
            while (scanner.hasNextLine()) {
                line = scanner.nextLine().strip();

                // Whole line comments
                if (line.startsWith("#")) {
                    lineNum++;
                    continue;
                }

                // Partial comments (end of line)
                if (line.contains("#")) {
                    line = line.split("#", 2)[0].strip();
                }

                if (line.length() == 0) {
                    lineNum++;
                    continue;
                }

                mapStr.add(line);

                // Are we currently in a directive?
                if (currentDirective == null) {
                    // Start a new one.
                    // Two cases:
                    // directiveName {   ....}
                    // directiveName: [attributes]
                    String[] lineAttrs = line.split("\\s+");

                    if (lineAttrs.length == 2 && lineAttrs[1].endsWith("{")) {
                        // Multi-line directive
                        // Split out the name now
                        currentDirective = lineAttrs[0];
                        directiveStart = lineNum;
                    } else {
                        // Single-line directive
                        for (int i = 1; i < lineAttrs.length; i++) {
                            final String kvRaw = lineAttrs[i];

                            if (!kvRaw.contains("=")) {
                                throw new IllegalArgumentException("On line " + lineNum + " while reading argument '" + kvRaw + "': Expected assignment (key=value)");
                            }

                            final String[] kv = kvRaw.split("=", 2);
                            attributes.add(new StringPair(kv[0], kv[1]));
                        }

                        endDirective(map, lineAttrs[0], attributes);
                        directiveStart = lineNum;
                        attributes.clear();
                    }
                } else {
                    // This is a multiline directive.
                    // Our possible options are an attribute (name    value) or end block.
                    if (line.equals("}")) {
                        // End the directive
                        endDirective(map, currentDirective, attributes);
                        attributes.clear();
                        currentDirective = null;
                    } else {
                        // There should be exactly one block of whitespace here
                        String[] kv = line.split("\\s+", 2);
                        attributes.add(new StringPair(kv[0].toLowerCase(), kv[1]));
                    }
                }

                lineNum++;
            }
        } catch (final Throwable t) {
            System.err.println("Error while parsing map!");
            System.err.println("Line " + lineNum + ": " + line);
            if (currentDirective != null)
                System.err.println("Directive: " + currentDirective + " at line " + directiveStart);
            throw t;
        }

        if (currentDirective != null) {
            throw new IllegalArgumentException("On line " + lineNum + ": Expected end of directive, but reached end of file instead.");
        }
    }

    private static void endDirective(final KillBillMap map, final String directiveName, final List<StringPair> attributes) {
        MapDirective directive = null;
        switch (directiveName.toLowerCase()) {
            case "chest":
                directive = new ChestDirective(chestId, attributes);
                chestId++;
                break;
            case "entity":
                directive = new EntityDirective(entityId, attributes);
                entityId++;
                break;
            case "object":
                directive = new ObjectDirective(objectId, attributes);
                objectId++;
                break;
            case "room":
                directive = new RoomDirective(attributes);
                break;
            case "tile":
                directive = new TileDirective(tileId, attributes);
                tileId++;
                break;
            case "config":
                if (hasConfig) throw new IllegalArgumentException("Cannot repeat the 'config' directive.");
                ConfigDirective config = new ConfigDirective(attributes);
                directive = config;
                map.setDisplayName(config.getMapName());
                hasConfig = true;
                break;
            default:
                throw new IllegalArgumentException("Invalid directive: " + directiveName);
        }

        map.addDirective(directive);

    }

    public static record StringPair(String key, String value) {
        public StringPair {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
        }
    }
}
