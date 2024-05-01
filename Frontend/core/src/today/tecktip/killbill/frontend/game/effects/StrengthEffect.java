package today.tecktip.killbill.frontend.game.effects;

import com.badlogic.gdx.graphics.Color;

import today.tecktip.killbill.frontend.KillBillGame;

/**
 * Strength effect, which applies a multiplier to the user's damage.
 * @author cs
 */
public class StrengthEffect extends Effect {

    /**
     * Damage multiplier
     */
    private float multiplier;

    /**
     * Creates a new strength effect.
     * @param duration Duration in seconds of the effect
     * @param multiplier Multiplier to apply to the user's damage
     */
    public StrengthEffect(final float duration, final float multiplier) {
        super("Strength", KillBillGame.get().getTextureLoader().get("ui_effects_strength"), duration, new Color(1, 0, 0, 0.5f));
        this.multiplier = multiplier;
    }

    /**
     * Gets the damage multiplier.
     * @return Factor to multiply the user's damage by.
     */
    public float getDamageMultiplier() {
        return multiplier;
    }
    
}
