package today.tecktip.killbill.frontend.game.items.potions;

import today.tecktip.killbill.common.maploader.ItemType;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.game.objects.Entity;

/**
 * Health potion, item that heals 1 health
 * @author cz
 */
public class HealthPotion extends Potion {

    /**
     * Creates a new health potion
     * @param name item name (Health Potion)
     * @param inventoryTexture texture in the hotbar
     * @param heldTexture texture in hand while holding the item
     * @param quantity how many of the item to give
     */
    public HealthPotion(int quantity) {
        super(ItemType.HEALTH_POTION, "Health Potion", KillBillGame.get().getTextureLoader().get("items_potion_health"), KillBillGame.get().getTextureLoader().get("items_potion_health_held"), "items_potion_health_held", quantity, 1);
    }

    @Override
    public boolean use(Entity user) {
        KillBillGame.get().getPlayer().setHealth(KillBillGame.get().getPlayer().getHealth() + 1);
        removeFromUser(user);
        return true;
    }
    
}
