package today.tecktip.killbill.common.maploader;

/**
 * Flags for features that objects should use.
 * @author cs
 */
public enum ObjectFlag {
    /**
     * Flags that an object processes keyboard inputs.
     */
    PROCESSES_INPUT,

    /**
     * Flags that an object needs to run a method before rendering starts.
     */
    NEEDS_PRE_RENDER_UPDATE,

    /**
     * If set, the object is collidable.
     */
    SOLID,

    /**
     * Freezes an entity.
     */
    FROZEN,

    /**
     * Processes interactions.
     */
    INTERACTABLE,

    /**
     * Can be attacked.
     */
    ATTACKABLE
}

