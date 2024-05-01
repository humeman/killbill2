package today.tecktip.killbill.frontend.game.items.weapons;

import today.tecktip.killbill.common.maploader.ItemType;

public  class Spear extends MeleeWeapon {

    public Spear() {
        super(
            ItemType.SPEAR,
            "Spear", 
            "items_weapon_spear",
            "items_weapon_spear_held",
            1,
            1,
            1
            );
    }
    
}
