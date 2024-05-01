package today.tecktip.killbill.common.maploader;

/**
 * A directive, or "block", defined in a map file.
 * @author cs
 */
public abstract class MapDirective {
    private final DirectiveType type;

    /**
     * Creates a new map directive.
     * @param type Type of this directive
     */
    protected MapDirective(final DirectiveType type) {
        this.type = type;
    }

    /**
     * Gets the directive type.
     * @return Type
     */
    public DirectiveType getType() {
        return type;
    }

    public static enum DirectiveType {
        OBJECT,
        TILE,
        ENTITY,
        ROOM,
        CHEST,
        CONFIG
    }
}
