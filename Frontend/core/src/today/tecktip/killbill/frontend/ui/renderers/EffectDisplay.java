package today.tecktip.killbill.frontend.ui.renderers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.frontend.KillBillGame;
import today.tecktip.killbill.frontend.game.effects.Effect;
import today.tecktip.killbill.frontend.game.objects.Player;

/**
 * An extension which draws a player's health to the screen
 * @author cs
 */
public class EffectDisplay extends RendererExtension {

    /**
     * The player to show the health of of.
     */
    private Player player;

    /**
     * Slider texture (stretched and tinted)
     */
    private Texture square;

    /**
     * Creates a new debug display.
     */
    public EffectDisplay(final Player player) {
        super(true);
        square = KillBillGame.get().getTextureLoader().get("ui_square");
        this.player = player;
    }

    @Override
    public void render(final SpriteBatch batch, final float delta) {
        int effectWidth = KillBillGame.get().getWidth() / 20;
        int spacing = KillBillGame.get().getWidth() / 200;
        int y = spacing;

        for (final Effect effect : player.getEffects()) {
            batch.draw(
                effect.getTexture(),
                spacing,
                y,
                effectWidth,
                effectWidth
            );

            Color oldColor = batch.getColor().cpy();
            batch.setColor(effect.getSliderColor());
            batch.draw(
                square,
                spacing,
                y,
                effectWidth,
                (int) (effectWidth * (effect.getRemainingDuration() / effect.getDuration())) 
            );
            batch.setColor(oldColor);

            y += effectWidth + spacing;
        }

        super.render(batch, delta);
    }
}
