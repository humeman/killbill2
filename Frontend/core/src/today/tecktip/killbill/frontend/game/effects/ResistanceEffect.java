package today.tecktip.killbill.frontend.game.effects;

import com.badlogic.gdx.graphics.Color;

import today.tecktip.killbill.frontend.KillBillGame;

/**
 * Resistance effect, which makes the player invincible for a given duration
 */
public class ResistanceEffect extends Effect {

    /**
     * creates a new resistance effect.
     * @param duration duration in seconds for effect to last
     */
    public ResistanceEffect(final float duration) {
        super("Resistance", KillBillGame.get().getTextureLoader().get("ui_effects_resistance"), duration, new Color(0, 1, 0, 0.5f));
    }
    
}
