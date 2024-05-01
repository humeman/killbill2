package today.tecktip.killbill.common.maploader.directives;

import java.util.List;
import java.util.regex.Pattern;

import today.tecktip.killbill.common.maploader.MapDirective;
import today.tecktip.killbill.common.maploader.MapLoader.StringPair;

/**
 * Directive for an import in a map.
 * @author cs
 */
public class ConfigDirective extends MapDirective {
    /**
     * RegEx pattern for valid map names.
     */
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9 ]*$"
    );

    private String mapName;

    public ConfigDirective(final List<StringPair> attributes) {
        super(DirectiveType.CONFIG);
        mapName = null;

        parseAttributes(attributes);
        validateAttributes();
    }

    private void parseAttributes(final List<StringPair> attributes) {
        for (final StringPair entry : attributes) {
            final String k = entry.key();
            final String v = entry.value();

            switch (k.toLowerCase()) {
                case "name":
                case "map_name":
                    if (mapName != null) throw new IllegalArgumentException("'name' is not repeatable.");
                    String name = v.replaceAll("_", " ");
                    if (!VALID_NAME_PATTERN.matcher(name).matches()) throw new IllegalArgumentException("Map names must be alphanumeric.");
                    mapName = name;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported attribute: " + k);
            }
        } 
    }

    private void validateAttributes() {
        if (mapName == null) {
            throw new IllegalArgumentException("'mapName' cannot be null.");
        }
    }

    public String getMapName() {
        return mapName;
    }
}
