package today.tecktip.killbill.common.maploader;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a map loaded from a ".kbmap" file.
 * @author cs
 */
public class KillBillMap {
    /**
     * List of map directives loaded from the file
     */
    private List<MapDirective> directives;

    /**
     * Display name for the map.
     */
    private String displayName;

    /**
     * String that parsed into this map.
     */
    private String mapStr;

    /**
     * Constructs an empty KillBillMap.
     */
    public KillBillMap() {
        directives = new ArrayList<>();
    }

    /**
     * Gets the directives in this map.
     * @return List of directives
     */
    public List<MapDirective> getDirectives() {
        return directives;
    }

    /**
     * Runs an operator on each directive in the map.
     * @param operator Operator to call
     */
    public void forEachDirective(final MapDirectiveOperator operator) {
        directives.forEach(directive -> {
            operator.run(directive);
        });
    }

    /**
     * Sets the original string that parsed into this map.
     * @param mapStr Map string
     */
    public void setString(final String mapStr) {
        this.mapStr = mapStr;
    }

    /**
     * Runs an operator on each directive in the map with the specified type.
     * @param <T> Type of directive
     * @param operator Operator to call
     * @param directiveClass Directive class to filter
     */
    public <T extends MapDirective> void forEachDirectiveOfType(final MapDirectiveOperatorOf<T> operator, final Class<T> directiveClass) {
        directives.forEach(directive -> {
            if (directiveClass.isInstance(directive)) {
                operator.run(directiveClass.cast(directive));
            }
        });
    }

    /**
     * Registers a new directive.
     * @param directive Directive
     */
    public void addDirective(final MapDirective directive) {
        directives.add(directive);
    }

    /**
     * Sets the display name for this map.
     * @param displayName
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the display name for this map.
     * @return Display name
     */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return mapStr;
    }

    public static interface MapDirectiveOperator {
        public void run(final MapDirective directive);
    }

    public static interface MapDirectiveOperatorOf<T extends MapDirective> {
        public void run(final T directive);
    }
}
