package today.tecktip.killbill.frontend.game.items.potions;

import today.tecktip.killbill.common.maploader.ItemType;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.game.effects.SpeedEffect;
import today.tecktip.killbill.frontend.game.objects.Entity;

/**
 * Speed potion, item that gives speed effect
 * @author cz
 */
public class SpeedPotion extends Potion {

    /**
     * Creates a new speed potion
     * @param quantity how many of item to give
     */
    public SpeedPotion(int quantity) {
        super(ItemType.SPEED_POTION, "Speed Potion", KillBillGame.get().getTextureLoader().get("items_potion_speed"), KillBillGame.get().getTextureLoader().get("items_potion_speed_held"), "items_potion_speed_held", quantity, 1);
    }

    /**
     * function to use the speed potion
     * @param user not used. Required to Override
     */
    @Override
    public boolean use(Entity user) {
        KillBillGame.get().getPlayer().applyEffect(new SpeedEffect(5, 2));
        removeFromUser(user);
        return true;
    }
    
}
