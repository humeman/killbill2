package today.tecktip.killbill.frontend.game.items.potions;

import com.badlogic.gdx.graphics.Texture;

import today.tecktip.killbill.common.maploader.ItemType;
import today.tecktip.killbill.frontend.game.items.Item;
import today.tecktip.killbill.frontend.game.objects.Entity;
import today.tecktip.killbill.frontend.game.objects.Player;

/**
 * basic representation of a potion item
 * @author cz
 */
public abstract class Potion extends Item {
    public Potion(ItemType type, String name, Texture inventoryTexture, Texture heldTexture, String heldTextureName, int quantity, float cooldown) {
        super(type, name, inventoryTexture, heldTexture, heldTextureName, quantity, cooldown);
    }

    protected void removeFromUser(final Entity user) {
        if (!(user instanceof Player)) return;

        Player p = (Player) user;

        p.removeItem(p.getIndexOfItem(this));
    }
}
