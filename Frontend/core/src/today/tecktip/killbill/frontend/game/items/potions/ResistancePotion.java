package today.tecktip.killbill.frontend.game.items.potions;

import today.tecktip.killbill.common.maploader.ItemType;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.game.effects.ResistanceEffect;
import today.tecktip.killbill.frontend.game.objects.Entity;

/**
 * Resistance potion, item that gives resistance effect
 * @author cz
 */
public class ResistancePotion extends Potion {

    /**
     * creates a new resisitance potion
     * @param quantity how many of item to give
     */
    public ResistancePotion(int quantity) {
        super(ItemType.RESISTANCE_POTION, "Resistance Potion", KillBillGame.get().getTextureLoader().get("items_potion_resistance"), KillBillGame.get().getTextureLoader().get("items_potion_resistance_held"), "items_potion_resistance_held", quantity, 1);
    }

    /**
     * function to use the resistance potion
     * @param user not used. Required to Override
     * @return 
     */
    @Override
    public boolean use(Entity user) {
        KillBillGame.get().getPlayer().applyEffect(new ResistanceEffect(5));
        removeFromUser(user);
        return true;
    }
    
}
