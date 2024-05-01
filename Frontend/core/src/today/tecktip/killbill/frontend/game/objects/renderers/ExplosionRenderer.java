package today.tecktip.killbill.frontend.game.objects.renderers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.game.items.Item;
import today.tecktip.killbill.frontend.game.objects.Entity;
import today.tecktip.killbill.frontend.game.objects.GameObject;

/**
 * Draws a gradually expanding & shrinking explosion as the texture for the object.
 * @author cs
 */
public class ExplosionRenderer extends ObjectRenderer {
    /**
     * The explosion texture.
     */
    private Texture texture;

    private float radius;

    private float timer;

    private boolean done;


    /**
     * Constructs a new explosion renderer.
     * @param texture Texture to draw
     * @param radius Radius of explosion in tiles (radius)
     */
    public ExplosionRenderer(final Texture texture, final float radius) {
        this.texture = texture;
        this.radius = radius;
        timer = 0;
        done = false;
    }

    public boolean isDone() {
        return done;
    }

    @Override
    public void renderTo(final float delta, final SpriteBatch batch, final GameObject object) {
        if (done) return;

        timer += delta;
        if (timer > 1f) {
            // All done
            done = true;
        }

        // Get the scale of the explosion
        float scale = timer / 0.5f; // Half a second each way

        if (scale > 1f) {
            // Going down in size
            scale = 2 - scale;
        }

        float size = radius * GlobalGameConfig.GRID_SIZE * scale;

        // Draw the explosion!
        batch.draw(texture, object.getRectangle().getCenterX() - size / 2, object.getRectangle().getCenterY() - size / 2, size, size);

    }

    @Override
    public void renderTo(final float delta, final SpriteBatch batch, final GameObject object, final Item item) {
        throw new UnsupportedOperationException("The explosion renderer cannot draw an item.");
    }

    @Override
    public void updateLocation(Entity entity) {
        // Do nothing :)
    }
}
