package today.tecktip.killbill.frontend.game.items;

import com.badlogic.gdx.graphics.Texture;

import today.tecktip.killbill.common.maploader.ItemType;
import today.tecktip.killbill.frontend.game.objects.Entity;

/**
 * Base representation of an inventory item.
 * @author cs
 */
public abstract class Item {
    /**
     * Display name of the item.
     */
    private final String name;

    /**
     * Item's type enum for UDP communication.
     */
    private final ItemType type;

    /**
     * The texture of this item as drawn in the inventory.
     */
    private final Texture inventoryTexture;

    /**
     * Texture as shown when held.
     */
    private final Texture heldTexture;

    /**
     * Asset name for the item shown when held (used for UDP sync).
     */
    private final String heldTextureName;

    /**
     * Number of items in the stack.
     */
    private int quantity;

    /**
     * Cooldown between uses.
     */
    private float cooldown;

    /**
     * Current remaining cooldown, if applicable.
     */
    private float currentCooldown;

    /**
     * Constructs a new item.
     * @param name Display name for the item
     * @param inventoryTexture Texture for the item shown in inventory
     * @param heldTexture Texture for the item shown when held
     * @param heldTexturename Asset name for the item shown when held (used for UDP sync)
     * @param quantity Quantity of the item
     * @param cooldown Cooldown in seconds between uses
     */
    public Item(final ItemType type, final String name, final Texture inventoryTexture, final Texture heldTexture, final String heldTextureName, final int quantity, final float cooldown) {
        this.type = type;
        this.name = name;
        this.heldTextureName = heldTextureName;
        this.inventoryTexture = inventoryTexture;
        this.heldTexture = heldTexture;
        this.quantity = quantity;
        this.cooldown = Math.max(cooldown, 0);
        this.currentCooldown = 0;
    }

    /**
     * Gets the item type enum representing this item.
     * @return Item type
     */
    public ItemType getType() {
        return type;
    }

    /**
     * Gets the display name for the object.
     * @return Display name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the inventory texture.
     * @return Texture as shown in user's inventory bar
     */
    public Texture getInventoryTexture() {
        return inventoryTexture;
    }

    /**
     * Gets the held texture.
     * @return Texture as shown in user's hand
     */
    public Texture getHeldTexture() {
        return heldTexture;
    }

    /**
     * Gets the held texture's name.
     * @return Asset name for the texture as shown in user's hand
     */
    public String getHeldTextureName() {
        return heldTextureName;
    }

    /**
     * Gets the quantity of items in this stack.
     * @return Quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Offsets the quantity of this item stack.
     * @param amount Amount to offset by
     * @return New amount
     */
    public int offsetQuantity(final int amount) {
        quantity += amount;

        return amount;
    }
    
    /**
     * Sets the quantity of this item stack.
     * @param amount Amount to set
     */
    public void setQuantity(final int amount) {
        quantity = amount;
    }

    public void beforeRender(final float delta) {
        if (currentCooldown > 0) {
            currentCooldown -= delta;
            if (currentCooldown < 0) currentCooldown = 0;
        }
    }

    /**
     * Called when used by the user.
     * @param user User that called this
     */
    public void runUse(final Entity user) {
        if (cooldown > 0) {
            // Check remaining cooldown
            if (currentCooldown > 0) {
                // Can't use yet
                return;
            }
        }

        if (use(user)) {
            resetCooldown();
        }
    }

    /**
     * Gets the overall cooldown applied between uses.
     * @return Cooldown in seconds
     */
    public float getCooldown() {
        return cooldown;
    }

    /**
     * Gets the current time left on the cooldown.
     * @return Remaining cooldown in seconds
     */
    public float getCurrentCooldown() {
        return currentCooldown;
    }

    /**
     * Resets the cooldown to its max value.
     */
    public void resetCooldown() {
        currentCooldown = cooldown;
    }

    /**
     * Uses the item.
     * @param user The user that is using the item
     * @return True if the item was used, false if skipped.
     */
    public abstract boolean use(final Entity user);
}
