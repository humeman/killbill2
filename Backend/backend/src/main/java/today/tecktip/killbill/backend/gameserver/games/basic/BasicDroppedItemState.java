package today.tecktip.killbill.backend.gameserver.games.basic;

import today.tecktip.killbill.common.gameserver.data.Coordinates;
import today.tecktip.killbill.common.maploader.ItemType;

/**
 * Represents an item that a user dropped.
 * @author cs
 */
public class BasicDroppedItemState {
    private String id;
    private Coordinates location;
    private ItemType type;
    private int quantity;

    /**
     * Constructs a new dropped item state for the BASIC game type.
     * @param parent Parent game state
     */
	public BasicDroppedItemState(final String id, final Coordinates location, final ItemType type, final int quantity) {
        this.id = id;
        this.location = location;
        this.type = type;
        this.quantity = quantity;
	}

    public String getId() {
        return id;
    }

    public Coordinates getLocation() {
        return location;
    }

    public ItemType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    
}
