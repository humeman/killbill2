package today.tecktip.killbill.frontend.game.items.weapons;

import today.tecktip.killbill.common.maploader.ItemType;

/**
 * A sword. Exciting, right????
 * @author cs
 */
public class Sword extends MeleeWeapon {
    public Sword() {
        super(
            ItemType.SWORD,
            "Sword",
            "items_weapon_sword",
            "items_weapon_sword_held",
            2,
            1.5f,
            1
        );
    }
}
