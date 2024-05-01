package today.tecktip.killbill.frontend.game.effects;

import com.badlogic.gdx.graphics.Color;

import today.tecktip.killbill.frontend.KillBillGame;

/**
 * Speed effect, which increases the players movement speed for a given duration by a given multiplier
 * @author Caleb Zea
 */
public class SpeedEffect extends Effect {

    /**
     * speed multipler
     */
    private float multiplier;

    /**
     * creates a new speed effect
     * @param duration duration in seconds for effect to last
     * @param multiplier multiplier to player movement speed
     */
    public SpeedEffect(final float duration, final float multiplier) {
        super("Speed", KillBillGame.get().getTextureLoader().get("ui_effects_speed"), duration, new Color(0, 0, 1f, 0.5f));
        this.multiplier = multiplier;
    }
    
    /**
     * Gets the speed multiplier
     * @return multiplier to player movement speed
     */
    public float getSpeedMultiplier() {
        return multiplier;
    }
}
