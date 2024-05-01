package today.tecktip.killbill.frontend.config;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

/**
 * Manages keybinds. In the future, this would be extended to load from the disk.
 * @author cs
 */
public class Keybinds {
    /**
     * The mappings of keys to integer codes.
     */
    private Map<KeyType, int[]> keys;

    /**
     * Sets up empty keybinds.
     */
    public Keybinds() {
        keys = new HashMap<>();
        // Boy do I love Java
        // Map.of() caps out way too early for us here
        keys.put(KeyType.UP, new int[] {Keys.W, Keys.UP});
        keys.put(KeyType.LEFT, new int[] {Keys.A, Keys.LEFT});
        keys.put(KeyType.RIGHT, new int[] {Keys.D, Keys.RIGHT});
        keys.put(KeyType.DOWN, new int[] {Keys.S, Keys.DOWN});
        keys.put(KeyType.SPRINT, new int[] {Keys.SHIFT_LEFT, Keys.SHIFT_RIGHT});
        keys.put(KeyType.INTERACT, new int[] {Keys.F, Keys.E});
        keys.put(KeyType.USE, new int[] {Keys.SPACE});
        keys.put(KeyType.DROP, new int[] {Keys.Q});
        keys.put(KeyType.CLOSE, new int[] {Keys.ESCAPE, Keys.END});
        keys.put(KeyType.TOGGLE_UI, new int[] {Keys.F1});
        keys.put(KeyType.DEBUG, new int[] {Keys.F3});
        keys.put(KeyType.RELOAD_MAP, new int[] {Keys.F5});
        keys.put(KeyType.NOCLIP, new int[] {Keys.F4});
        keys.put(KeyType.CHAT, new int[] {Keys.ENTER});
        keys.put(KeyType.INVENTORY_1, new int[] {Keys.NUM_1, Keys.NUMPAD_1});
        keys.put(KeyType.INVENTORY_2, new int[] {Keys.NUM_2, Keys.NUMPAD_2});
        keys.put(KeyType.INVENTORY_3, new int[] {Keys.NUM_3, Keys.NUMPAD_3});
        keys.put(KeyType.INVENTORY_4, new int[] {Keys.NUM_4, Keys.NUMPAD_4});
        keys.put(KeyType.INVENTORY_5, new int[] {Keys.NUM_5, Keys.NUMPAD_5});
        keys.put(KeyType.INVENTORY_6, new int[] {Keys.NUM_6, Keys.NUMPAD_6});
        keys.put(KeyType.INVENTORY_7, new int[] {Keys.NUM_7, Keys.NUMPAD_7});
        keys.put(KeyType.INVENTORY_8, new int[] {Keys.NUM_8, Keys.NUMPAD_8});
        keys.put(KeyType.INVENTORY_9, new int[] {Keys.NUM_9, Keys.NUMPAD_9});
    }

    /**
     * Gets the keys bound to a specific key type.
     * @param keyType Key type the keys are bound to
     * @return Key codes associated with the key
     */
    public int[] getKeys(final KeyType keyType) {
        return keys.get(keyType);
    }

    /**
     * Returns true if any of the bound keys are pressed right now.
     * @param keyType The key type to check
     * @return True if any are pressed
     */
    public boolean isKeyPressed(final KeyType keyType) {
        for (final int keyCode : getKeys(keyType)) {
            if (Gdx.input.isKeyPressed(keyCode)) return true;
        }
        return false;
    }

    /**
     * Returns true if any of the bound keys were just pressed.
     * @param keyType The key type to check
     * @return True if any was just pressed (won't return true next time)
     */
    public boolean isKeyJustPressed(final KeyType keyType) {
        for (final int keyCode : getKeys(keyType)) {
            if (Gdx.input.isKeyJustPressed(keyCode)) return true;
        }
        return false;
    }

    /**
     * Checks if a key is a member of a particular key group.
     * @param keyType Key type to check
     * @param key Key code to compare
     * @return True if the key is a member of that key type
     */
    public boolean isMemberOf(final KeyType keyType, final int key) {
        for (final int keyCode : getKeys(keyType)) {
            if (keyCode == key) return true;
        }
        return false;
    }


    /**
     * The types of keys we care about.
     */
    public enum KeyType {
        /**
         * Move player forward
         */
        UP,

        /**
         * Move player left
         */
        LEFT,

        /**
         * Move player right
         */
        RIGHT,

        /**
         * Move player down
         */
        DOWN,

        /**
         * Make player sprint
         */
        SPRINT,

        /**
         * Interact with an object
         */
        INTERACT,

        /**
         * Use the held item
         */
        USE,

        /**
         * Drop the held item
         */
        DROP,

        /**
         * Closes the game
         */
        CLOSE,

        /**
         * Toggles the UI visibility state
         */
        TOGGLE_UI,

        /**
         * Opens the debug display
         */
        DEBUG,

        /**
         * Reloads the current map (in DevGameScreen only)
         */
        RELOAD_MAP,

        /**
         * Disables collisions (in DevGameScreen only)
         */
        NOCLIP,

        /**
         * Opens the chat box
         */
        CHAT,

        /**
         * Inventory slot 1
         */
        INVENTORY_1,

        /**
         * Inventory slot 2
         */
        INVENTORY_2,

        /**
         * Inventory slot 3
         */
        INVENTORY_3,

        /**
         * Inventory slot 4
         */
        INVENTORY_4,

        /**
         * Inventory slot 5
         */
        INVENTORY_5,
        
        /**
         * Inventory slot 6
         */
        INVENTORY_6,

        /**
         * Inventory slot 7
         */
        INVENTORY_7,

        /**
         * Inventory slot 8
         */
        INVENTORY_8,

        /**
         * Inventory slot 9
         */
        INVENTORY_9
    }
}
