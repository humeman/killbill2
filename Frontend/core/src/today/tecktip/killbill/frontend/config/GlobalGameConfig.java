package today.tecktip.killbill.frontend.config;

import com.badlogic.gdx.graphics.Color;

import today.tecktip.killbill.frontend.config.Keybinds.KeyType;

 /**
 * Global static config values used across the game.
 * @author cs
 */
public abstract class GlobalGameConfig {
    /**
     * The size of each block on the grid in pixels.
     */
    public static final int GRID_SIZE = 16;

    /**
     * The number of pixels the player can move each second.
     */
    public static final int PLAYER_PIXELS_PER_SECOND = 48;

    /**
     * The speed multiplier when the player is sprinting.
     */
    public static final float SPRINT_MULTIPLIER = 1.35f;

    /**
     * X world size in tiles.
     */
    public static final int WORLD_SIZE_X_TILES = 1000;

    /**
     * Y world size in tiles.
     */
    public static final int WORLD_SIZE_Y_TILES = 1000;

    /**
     * Inventory slots
     */
    public static final int INVENTORY_SIZE = 5;

    /**
     * Knockback speed (initial, decays to 0 over {@link #KNOCKBACK_DURATION})
     */
    public static final int KNOCKBACK_SPEED = 3;

    /**
     * Knockback speed (initial, decays to 0 over {@link #KNOCKBACK_DURATION})
     */
    public static final float KNOCKBACK_DURATION = 0.25f;

    /**
     * Key type array for inventory hotkeys. Must contain {@link #INVENTORY_SIZE} elements.
     */
    public static final KeyType[] INVENTORY_KEYS = new KeyType[] {
        KeyType.INVENTORY_1,
        KeyType.INVENTORY_2,
        KeyType.INVENTORY_3,
        KeyType.INVENTORY_4,
        KeyType.INVENTORY_5
    };

    /**
     * Entity reach in pixels.
     */
    public static final int REACH_PX = 24;

    /**
     * The angles that will be checked in interactions.
     */
    public static final int[] INTERACT_ANGLES = new int[] {
        -45,
        -22,
        0,
        22,
        45
    };

    /**
     * Player health points.
     */
    public static final int PLAYER_HEALTH = 10;

    /**
     * Maximum seconds to wait for a UDP connection before failing.
     */
    public static final float UDP_CONNECT_TIMEOUT = 15;

    /**
     * Primary blood-red color used in logos.
     */
    public static final Color PRIMARY_COLOR = new Color(255.0f / 255, 8.0f / 255, 8.0f / 255, 1.0f);

    /**
     * Secondary shadow color used in logos.
     */
    public static final Color SECONDARY_COLOR = new Color(60.0f / 255, 0.0f / 255, 0.0f / 255, 1.0f);

    /**
     * Tertiary alternate color used in logos.
     */
    public static final Color TERTIARY_COLOR = new Color(255.0f / 255, 170.0f / 255, 170.0f / 255, 1.0f);

    /**
     * Enables extra verbose debug messages.
     */
    public static final boolean DEBUG = false;

    /**
     * The minimum tiles a user must move before it will be sent to the server.
     */
    public static final float MIN_MOVE_PER_SEND = 0.2f;
}
