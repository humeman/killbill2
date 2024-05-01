package today.tecktip.killbill.frontend.game.items.weapons;

import today.tecktip.killbill.common.maploader.ItemType;

public class Axe extends MeleeWeapon{

    public Axe() {
        super(
            ItemType.AXE,
            "Axe", 
            "items_weapon_axe",
            "items_weapon_axe_held", 
            2, 
            2, 
            1
            );
    }
    
}
