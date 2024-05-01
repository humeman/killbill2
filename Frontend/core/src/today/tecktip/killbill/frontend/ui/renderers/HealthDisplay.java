package today.tecktip.killbill.frontend.ui.renderers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.game.objects.Player;

/**
 * An extension which draws a player's health to the screen
 * @author cs
 */
public class HealthDisplay extends RendererExtension {

    /**
     * The player to show the health of of.
     */
    private Player player;

    /**
     * Full heart
     */
    private Texture fullHeartTexture;

    /**
     * Half heart
     */
    private Texture halfHeartTexture;

    /**
     * No heart
     */
    private Texture noHeartTexture;

    /**
     * Creates a new debug display.
     */
    public HealthDisplay(final Player player) {
        super(true);
        fullHeartTexture = KillBillGame.get().getTextureLoader().get("ui_hud_heart");
        halfHeartTexture = KillBillGame.get().getTextureLoader().get("ui_hud_half_heart");
        noHeartTexture = KillBillGame.get().getTextureLoader().get("ui_hud_no_heart");
        this.player = player;
    }

    @Override
    public void render(final SpriteBatch batch, final float delta) {
        int offset = KillBillGame.get().getWidth() / 20;
        int heartWidth = KillBillGame.get().getWidth() * 12 / 400;
        int spacing = KillBillGame.get().getWidth() / 200;
        int x = (KillBillGame.get().getWidth() - heartWidth * (player.getMaxHealth() / 2) - spacing * (player.getMaxHealth() / 2 - 1)) / 2;

        int state = 2;
        for (int i = 2; i <= player.getMaxHealth(); i += 2) {
            if (player.getHealth() >= i) {
                // Full heart
                state = 2;
            } else if (player.getHealth() == i - 1) {
                // Half heart
                state = 1;
            } else {
                // No heart
                state = 0;
            }
            
            if (i % 2 == 0) {
                batch.draw(
                    state == 0 ? noHeartTexture : (state == 1 ? halfHeartTexture : fullHeartTexture),
                    x,
                    offset + 2 * spacing,
                    heartWidth,
                    heartWidth
                );
                state = 0;

                x += heartWidth + spacing;
            }
        }

        super.render(batch, delta);
    }
}
