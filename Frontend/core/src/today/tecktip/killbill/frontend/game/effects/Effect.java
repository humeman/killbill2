package today.tecktip.killbill.frontend.game.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

/**
 * Base representation of a player effect.
 * @author cs
 */
public abstract class Effect {
    /**
     * Display name of the effect.
     */
    private final String name;

    /**
     * The texture of this effect as drawn in the corner.
     */
    private final Texture texture;

    /**
     * Total duration of the effect.
     */
    private float duration;

    /**
     * Remaining time of the effect.
     */
    private float remainingDuration;

    /**
     * Color of the slider.
     */
    private Color sliderColor;

    /**
     * Constructs a new item.
     * @param name Display name for the item
     * @param texture Texture for the item shown on the UI
     * @param duration Duration of the effect in seconds
     * @param sliderColor Slider color
     */
    public Effect(final String name, final Texture texture, float duration, final Color sliderColor) {
        this.name = name;
        this.texture = texture;
        this.duration = duration;
        this.remainingDuration = duration;
        this.sliderColor = sliderColor;
    }

    /**
     * Gets the display name for the effect.
     * @return Display name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the UI texture.
     * @return Texture as shown in the bottom corner of the screen
     */
    public Texture getTexture() {
        return texture;
    }

    /**
     * Gets a copy of the slider color.
     * @return Slider color copy
     */
    public Color getSliderColor() {
        return sliderColor.cpy();
    }

    /**
     * Gets the duration of this effect (when it started).
     * @return Duration
     */
    public float getDuration() {
        return duration;
    }

    /**
     * Gets the remaining time on this effect.
     * @return Remaining time
     */
    public float getRemainingDuration() {
        return remainingDuration;
    }

    /**
     * Updates the duration on the effect.
     * @param delta Time since last frame
     * @return True = effect has ended
     */
    public boolean update(final float delta) {
        remainingDuration -= delta;

        return remainingDuration <= 0;
    }
}
