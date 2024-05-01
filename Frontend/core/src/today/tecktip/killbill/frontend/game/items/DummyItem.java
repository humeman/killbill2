package today.tecktip.killbill.frontend.game.items;

import com.badlogic.gdx.graphics.Texture;

import today.tecktip.killbill.common.maploader.ItemType;
import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.game.objects.Entity;

/**
 * Dummy representation of an inventory item for UDP.
 * @author cs
 */
public class DummyItem extends Item {

    /**
     * Constructs a new dummy item.
     * @param heldTexture Texture for the item shown when held
     * @param heldTexturename Asset name for the item shown when held (used for UDP sync)
     */
    public DummyItem(final Texture heldTexture, final String heldTextureName) {
        super(
            ItemType.SWORD,
            "Dummy",
            KillBillGame.get().getTextureLoader().get("default"),
            heldTexture,
            heldTextureName,
            1,
            0f
        );
    }

    @Override
    public boolean use(final Entity user) {
        return false;
    }
}
