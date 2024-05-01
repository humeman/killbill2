package today.tecktip.killbill.frontend.game.items.weapons;

import today.tecktip.killbill.common.maploader.ItemType;

public class Hammer extends MeleeWeapon {

    public Hammer() {
        super(
            ItemType.HAMMER,
            "Hammer", 
            "items_weapon_hammer", 
            "items_weapon_hammer_held",
            3, 
            3, 
            1
            );
    }
    
}
