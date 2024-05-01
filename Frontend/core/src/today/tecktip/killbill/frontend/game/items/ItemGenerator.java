package today.tecktip.killbill.frontend.game.items;

import today.tecktip.killbill.common.maploader.ItemType;
import today.tecktip.killbill.frontend.exceptions.CatastrophicException;
import today.tecktip.killbill.frontend.game.items.potions.HealthPotion;
import today.tecktip.killbill.frontend.game.items.potions.ResistancePotion;
import today.tecktip.killbill.frontend.game.items.potions.SpeedPotion;
import today.tecktip.killbill.frontend.game.items.potions.StrengthPotion;
import today.tecktip.killbill.frontend.game.items.weapons.Axe;
import today.tecktip.killbill.frontend.game.items.weapons.Computer;
import today.tecktip.killbill.frontend.game.items.weapons.Hammer;
import today.tecktip.killbill.frontend.game.items.weapons.PenguinLauncher;
import today.tecktip.killbill.frontend.game.items.weapons.Plushie;
import today.tecktip.killbill.frontend.game.items.weapons.RamLauncher;
import today.tecktip.killbill.frontend.game.items.weapons.Spear;
import today.tecktip.killbill.frontend.game.items.weapons.SummoningWand;
import today.tecktip.killbill.frontend.game.items.weapons.Sword;

/**
 * Generates items from their enum references.
 * @author cs
 */
public class ItemGenerator {
    /**
     * Generates a new item for the specified item type.
     * @param itemType Item type
     * @return New item instance
     */
    public static Item generate(final ItemType itemType) {
        switch (itemType) {
            case AXE:
                return new Axe();
            case HAMMER:
                return new Hammer();
            case HEALTH_POTION:
                return new HealthPotion(1);
            case RESISTANCE_POTION:
                return new ResistancePotion(1);
            case SPEAR:
                return new Spear();
            case SPEED_POTION:
                return new SpeedPotion(1);
            case STRENGTH_POTION:
                return new StrengthPotion(1);
            case SWORD:
                return new Sword();
            case PLUSHIE:
                return new Plushie();
            case COMPUTER:
                return new Computer();
            case PENGUIN_LAUNCHER:
                return new PenguinLauncher();
            case RAM_LAUNCHER:
                return new RamLauncher();
            case SUMMONING_WAND:
                return new SummoningWand();
            default:
                throw new CatastrophicException("Missing item type: " + itemType);
        }
    }
}
