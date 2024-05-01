package today.tecktip.killbill.frontend.game.items.potions;

import today.tecktip.killbill.common.maploader.ItemType;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.game.objects.Entity;
import today.tecktip.killbill.frontend.game.effects.StrengthEffect;

/**
 * Strength Potion, item that gives damage multiplier
 * @author cz
 */
public class StrengthPotion extends Potion {

    /**
     * Creates a new strength potion
     * @param quantity how many of the item to give
     */
    public StrengthPotion(int quantity) {
        super(ItemType.STRENGTH_POTION, "Strength Potion", KillBillGame.get().getTextureLoader().get("items_potion_strength"), KillBillGame.get().getTextureLoader().get("items_potion_strength_held"), "items_potion_strength_held", quantity, 1);
    }

    /**
     * Gives the player a strength effect, increasing their damage by 2
     * @param user not used. Required to Override
     */
    @Override
    public boolean use(Entity user) {
        KillBillGame.get().getPlayer().applyEffect(new StrengthEffect(5, 2));
        removeFromUser(user);
        return true;
    }
    
}
