package today.tecktip.killbill.frontend.game.objects.renderers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import today.tecktip.killbill.frontend.config.GlobalGameConfig;
import today.tecktip.killbill.frontend.game.items.Item;
import today.tecktip.killbill.frontend.game.objects.Entity;
import today.tecktip.killbill.frontend.game.objects.GameObject;
import today.tecktip.killbill.frontend.util.MathUtil;

/**
 * Extensible class allowing us to render objects in various ways.
 * @author cs
 */
public abstract class ObjectRenderer {
    /**
     * Renders the object to the specified sprite batch.
     * @param delta Delta time since last render
     * @param batch Sprite batch to draw to
     * @param object Object to draw
     */
    public abstract void renderTo(final float delta, final SpriteBatch batch, final GameObject object);
    
    /**
     * Renders the object to the specified sprite batch with a held item.
     * @param delta Delta time since last render
     * @param batch Sprite batch to draw to
     * @param object Object to draw
     * @param heldItem Item to render as being held
     */
    public abstract void renderTo(final float delta, final SpriteBatch batch, final GameObject object, final Item heldItem);
    
    /**
     * Updates locations when the provided entity moves.
     * @param entity Entity that is being rendered
     */
    public abstract void updateLocation(final Entity entity);

    /**
     * Draws a bounding box around the object.
     * @param renderer Shape renderer
     * @param object Object
     */
    public void drawBoundingBox(final ShapeRenderer renderer, final GameObject object) {
        renderer.rect(object.getRectangle().getX(), object.getRectangle().getY(), object.getRectangle().getWidth(), object.getRectangle().getHeight());
    }

    /**
     * Draws an entity's looking line.
     * @param renderer Shape renderer
     * @param entity Entity
     */
    public void drawLookingLine(final ShapeRenderer renderer, final Entity entity) {
        float distanceOffset = Math.abs(MathUtil.distanceToEdgeAtAngle(entity.getRectangle().getWidth(), entity.getRotation() + 90));

        float x0 = entity.lookingLineX(distanceOffset, 0);
        float y0 = entity.lookingLineY(distanceOffset, 0);
        float x1 = entity.lookingLineX(distanceOffset + GlobalGameConfig.REACH_PX, 0);
        float y1 = entity.lookingLineY(distanceOffset + GlobalGameConfig.REACH_PX, 0);

        renderer.line(x0, y0, x1, y1);
    }
}
