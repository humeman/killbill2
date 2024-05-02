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
     * Constructs an empty game renderer.
     */
    public GameRenderer() {
        objects = new ArrayList<>();
        toAdd = new ArrayList<>();
        toRemove = new ArrayList<>();
    }

    /**
     * Adds an object to the renderer.
     * @param object Object to add
     */
    public synchronized void addObject(final GameObject object) {
        toAdd.add(object);
    }

    /**
     * Removes an object from the renderer.
     * @param operator Object to remove
     */
    public synchronized void removeObject(final GameObject object) {
        toRemove.add(object);
    }

    /**
     * Runs a method on each object in the renderer.
     * @param operator Operator to call on each object.
     * @return True if an operation returned true (cancelled future executions)
     */
    public synchronized boolean forEachObject(final GameObjectOperatorMethod operator) {
        for (final GameObject object : objects) {
            if (operator.run(object)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void clearObjects() {
        objects.clear();
    }

    /**
     * Renders all objects in the renderer to a batch.
     * @param batch Sprite batch to draw to
     * @param delta Time since last render
     */
    public synchronized void render(final SpriteBatch batch, final float delta) {
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

        for (final GameObject object : objects) {
            object.renderTo(delta, batch);
        }
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
