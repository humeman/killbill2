package today.tecktip.killbill.frontend.game.objects;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.common.maploader.ObjectFlag;
import today.tecktip.killbill.frontend.game.objects.renderers.ObjectRenderer;
import today.tecktip.killbill.frontend.ui.Rectangle;

/**
 * Base class for an object that can be rendered on the game grid.
 * @author cs
 */
public abstract class GameObject {
    /**
     * The renderer that draws this object.
     */
    protected ObjectRenderer renderer;

    /**
     * The flags this object uses.
     */
    protected final List<ObjectFlag> flags;

    /**
     * The rotation in degrees of this object. 90 = up (I think)
     */
    protected int rotation;

    /**
     * Constructs a blank game object.
     * @param renderer The renderer to draw the object with
     * @param List of default flags
     */
    public GameObject(final ObjectRenderer renderer, final ObjectFlag[] flags) {
        this.renderer = renderer;
        this.flags = new ArrayList<>();
        for (final ObjectFlag flag : flags) this.flags.add(flag);
        this.rotation = 0;
    }

    /**
     * Constructs a blank game object.
     * @param renderer The renderer to draw the object with
     * @param flags Modifiable list of flags to apply
     */
    public GameObject(final ObjectRenderer renderer, final List<ObjectFlag> flags) {
        this.renderer = renderer;
        this.flags = flags;
        this.rotation = 0;
    }

    /**
     * Gets this object's renderer.
     * @return Object renderer
     */
    public ObjectRenderer getRenderer() {
        return renderer;
    }

    /**
     * Changes the object's renderer.
     * @param renderer New renderer
     */
    public void setRenderer(final ObjectRenderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Gets the rectangle that bounds this object.
     * @return Rectangle
     */
    public abstract Rectangle getRectangle();

    /**
     * Renders this object to the specified batch.
     * @param batch Batch to render to
     */
    public abstract void renderTo(final float delta, final SpriteBatch batch);

    /**
     * Executed when an entity interacts with this object.
     * @param entity Entity that interacted with this object
     * @return True to cancel any other interactions that were matched
     */
    public abstract boolean onInteract(final Entity entity);

    /**
     * Checks if the object is colliding with another object.
     * @param object Object to check collision with
     * @return True if colliding
     */
    public boolean isCollidingWith(final GameObject object) {
        return getRectangle().overlaps(object.getRectangle());
    }

    /**
     * Checks if the object would be colliding with another object if it were to move
     *  to the specified coordinates.
     * @param object Object to check collision with
     * @param x X coordinate to check at
     * @param y Y coordinate to check at
     * @return True if colliding
     */
    public boolean wouldCollideAt(final GameObject object, final float x, final float y) {
        return getRectangle().overlapsAt(object.getRectangle(), x, y);
    }

    /**
     * Checks if another point is contained in this object.
     * @param x X coordinate
     * @param y Y coordinate
     * @return True if contained within this object's rectangle
     */
    public boolean containsPoint(final float x, final float y) {
        return getRectangle().containsPoint(x, y);
    }

    /**
     * Checks if an object flag is set.
     * @param flag Flag to check
     * @return True if set, false if not
     */
    public boolean hasFlag(final ObjectFlag flag) {
        for (final ObjectFlag gFlag : flags) {
            if (flag.equals(gFlag)) return true;
        }
        return false;
    }

    /**
     * Override this method if the {@link ObjectFlag#PROCESSES_INPUT} is set.
     * <p>
     * Processes a keypress.
     * @param key The key that was pressed (see GDX Keys class).
     * @return Whether input was processed. If false is returned, the input will be processed by future objects.
     */
    public boolean processInput(final int key) {
        return false;
    }

    /**
     * Override this method if the {@link ObjectFlag#NEEDS_PRE_RENDER_UPDATE} is set.
     * <p>
     * Performs pre-render tasks.
     * @param delta The time in seconds since the last frame was rendered.
     */
    public void beforeRender(final float delta) {}

    /**
     * Gets the rotation of the object.
     * @return
     */
    public int getRotation() {
        return rotation;
    }

    public void setRotation(final int rotation) {
        this.rotation = rotation;
    }

    public void rotate(final int offset) {
        rotation += offset;
    }

    public void setFlag(final ObjectFlag flag) {
        if (!flags.contains(flag)) flags.add(flag);
    }

    public void unsetFlag(final ObjectFlag flag) {
        flags.remove(flag);
    }
}
