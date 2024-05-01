package today.tecktip.killbill.frontend.game.items.weapons;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.game.items.Item;
import today.tecktip.killbill.frontend.game.objects.Entity;
import today.tecktip.killbill.common.maploader.ItemType;
import today.tecktip.killbill.common.maploader.ObjectFlag;
import today.tecktip.killbill.frontend.game.objects.Player;
import today.tecktip.killbill.frontend.screens.GameScreen;

/**
 * A melee weapon.
 * @author cs
 */
public abstract class MeleeWeapon extends Item {

    private final int damage;

    /**
     * Creates a new melee weapon.
     * @param name Display name
     * @param texture Inventory texture
     * @param heldTexture Hand texture
     * @param damage Damage dealt per hit
     * @param cooldown Cooldown between uses in seconds
     * @param quantity Quantity of item
     */
    public MeleeWeapon(final ItemType type, final String name, final String texture, final String heldTexture, final int damage, final float cooldown, final int quantity) {
        super(
            type,
            name,
            KillBillGame.get().getTextureLoader().get(texture),
            KillBillGame.get().getTextureLoader().get(heldTexture),
            heldTexture,
            quantity,
            cooldown
        );
        this.damage = damage;
    }

    @Override
    public boolean use(final Entity user) {
        if (user != KillBillGame.get().getPlayer()) return false;

        final Player p = (Player) user;

        // Run attacks on any other entities
        if (!(KillBillGame.get().getCurrentScreen() instanceof GameScreen)) return false;
        GameScreen screen = (GameScreen) KillBillGame.get().getCurrentScreen();

        screen.getGameRenderer().forEachObject(
            object -> {
                if (object == user) return false;
                if (object instanceof Entity && object.hasFlag(ObjectFlag.ATTACKABLE)) {
                    final Entity e = (Entity) object;
                    // Check if in range
                    if (p.canInteractWith(e)) {
                        e.damage(damage);
                    }
                }
                return false;
            }
        );

        return true;
    }
}
