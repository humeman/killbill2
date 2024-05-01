package today.tecktip.killbill.frontend.game;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.frontend.game.objects.GameObject;

/**
 * Renders the game area to the screen.
 */
public class GameRenderer {
    /**
     * The objects to be rendered to the screen.
     */
    private List<GameObject> objects;

    /**
     * Items to add after the next render cycle.
     */
    private List<GameObject> toAdd;

    /**
     * Items to remove after the next render cycle.
     */
    private List<GameObject> toRemove;

    /**
     * Notes if the list is currently being iterated through to prevent concurrent modification.
     */
    private boolean isIterating;

    /**
     * Constructs an empty game renderer.
     */
    public GameRenderer() {
        isIterating = false;
        objects = new ArrayList<>();
        toAdd = new ArrayList<>();
        toRemove = new ArrayList<>();
    }

    /**
     * Adds an object to the renderer.
     * @param object Object to add
     */
    public void addObject(final GameObject object) {
        if (isIterating) toAdd.add(object);
        else objects.add(object);
    }

    /**
     * Removes an object from the renderer.
     * @param operator Object to remove
     */
    public void removeObject(final GameObject object) {
        if (isIterating) toRemove.add(object);
        else objects.remove(object);
    }

    /**
     * Runs a method on each object in the renderer.
     * @param operator Operator to call on each object.
     * @return True if an operation returned true (cancelled future executions)
     */
    public boolean forEachObject(final GameObjectOperatorMethod operator) {
        isIterating = true;
        for (final GameObject object : objects) {
            if (operator.run(object)) {
                isIterating = false;
                return true;
            }
        }
        isIterating = false;
        return false;
    }

    /**
     * Gets the internal list used for this renderer's objects.
     * <p>
     * Probably don't add objects this way...
     * @return Object list
     */
    public List<GameObject> getObjects() {
        return objects;
    }

    /**
     * Renders all objects in the renderer to a batch.
     * @param batch Sprite batch to draw to
     * @param delta Time since last render
     */
    public void render(final SpriteBatch batch, final float delta) {
        if (toAdd.size() != 0) {
            for (final GameObject o : toAdd) {
                objects.add(o);
            }
            toAdd.clear();
        }
        if (toRemove.size() != 0) {
            for (final GameObject o : toRemove) {
                objects.remove(o);
            }
            toRemove.clear();
        }

        isIterating = true;
        for (final GameObject object : objects) {
            object.renderTo(delta, batch);
        }
        isIterating = false;
    }

    /**
     * A functional interface for operating on game objects.
     */
    public static interface GameObjectOperatorMethod {
        /**
         * A method to run on an object.
         * @param object The object to operate on
         * @return Whether to stop operating. If true is returned, any future objects down the list are not operated on.
         */
        public boolean run(final GameObject object);
    }
}
