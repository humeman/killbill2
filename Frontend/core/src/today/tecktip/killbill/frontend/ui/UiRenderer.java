package today.tecktip.killbill.frontend.ui;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.frontend.ui.renderers.RendererExtension;

/**
 * Renders a UI, like a menu or HUD.
 * @author cs
 */
public class UiRenderer {
    
    /**
     * All elements registered to this renderer.
     */
    private final List<UiElement> elements;

    /**
     * Extensible renderers to also run.
     */
    private final List<RendererExtension> renderers;

    /**
     * Constructs an empty UiRenderer.
     */
    public UiRenderer() {
        elements = new ArrayList<>();
        renderers = new ArrayList<>();
    }

    /**
     * Adds an element to be rendered by this renderer.
     * @param element Element to add
     */
    public void add(final UiElement element) {
        elements.add(element);
    }

    /**
     * Adds an extension to be rendered by this renderer.
     * @param extension Extension to register
     */
    public void add(final RendererExtension extension) {
        renderers.add(extension);
    }

    /**
     * Removes an element from this renderer.
     * @param element
     */
    public void remove(final UiElement element) {
        elements.remove(element);
    }

    /**
     * Removes an extension currently being rendered by this renderer.
     * @param extension Extension to remove
     */
    public void remove(final RendererExtension extension) {
        renderers.remove(extension);
    }

    /**
     * Gets the list of all elements registered to this renderer.
     * @return Elements
     */
    public List<UiElement> getElements() {
        return elements;
    }

    /**
     * Renders all elements to the UI.
     * @param batch Sprite batch to render to
     * @param delta Time in seconds since last render
     */
    public void render(final SpriteBatch batch, final float delta) {
        // Pre-render tasks
        for (final RendererExtension extension : renderers) {
            if (extension.isEnabled()) extension.render(batch, delta);
        }

        for (final UiElement element : elements) {
            if (!element.isVisible()) continue;

            element.render(batch, delta);
        }
    }

    /**
     * Runs the click method on the topmost element containing the specified X and Y (if one exists).
     * @param x X coordinate of pointer
     * @param y Y coordinate of pointer
     * @return True if an element was clicked
     */
    public boolean runClick(final float x, final float y) {
        for (final RendererExtension extension : renderers) {
            if (extension.isEnabled()) {
                if (extension.runClick(x, y)) return true;
            }
        }

        // Run in reverse so the topmost element is picked (in case of collisions)
        for (int i = elements.size() - 1; i >= 0; i--) {
            final UiElement element = elements.get(i);

            if (!element.isVisible()) continue;

            if (element.getRectangle() == null) continue;

            if (element.getRectangle().containsPoint(x, y)) {
                element.onClicked();
                return true;
            }
        }
        return false;
    }

    /**
     * Runs the unclick method on the topmost element containing the specified X and Y (if one exists).
     * @param x X coordinate of pointer
     * @param y Y coordinate of pointer
     * @return True if an element was clicked
     */
    public boolean runUnclick(final float x, final float y) {
        for (final RendererExtension extension : renderers) {
            if (extension.isEnabled()) {
                if (extension.runUnclick(x, y)) return true;
            }
        }

        // Run in reverse so the topmost element is picked (in case of collisions)
        for (int i = elements.size() - 1; i >= 0; i--) {
            final UiElement element = elements.get(i);

            if (element.getRectangle().containsPoint(x, y)) {
                element.onUnclicked();
                return true;
            }
        }
        return false;
    }

    /**
     * Runs the keypress method on each element in order of newest to oldest.
     * @param key Key that was pressed (corresponding to GDX's {@link Keys} class)
     * @return True if an element processed the keypress
     */
    public boolean runKey(final int key) {
        for (final RendererExtension extension : renderers) {
            if (extension.isEnabled()) {
                extension.runKey(key);
            }
        }

        // Run in reverse so the topmost element is picked (in case of collisions)
        for (int i = elements.size() - 1; i >= 0; i--) {
            final UiElement element = elements.get(i);

            if (!element.isVisible()) continue;

            if (element.onKeyPressed(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Runs the key typed method on each element in order of newest to oldest.
     * <p>
     * Should take priority over {@link #runKey(int)}.
     * @param c Character that was typed
     * @return True if an element processed the key type
     */
    public boolean runTyped(final char c) {
        for (final RendererExtension extension : renderers) {
            if (extension.isEnabled()) {
                extension.runTyped(c);
            }
        }

        // Run in reverse so the topmost element is picked (in case of collisions)
        for (int i = elements.size() - 1; i >= 0; i--) {
            final UiElement element = elements.get(i);

            if (!element.isVisible()) continue;

            if (element.onKeyTyped(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells all elements that the screen dimensions changed.
     */
    public void resize() {
        for (final RendererExtension extension : renderers) {
            extension.resize();
        }

        for (final UiElement element : elements) {
            element.onResize();
        }
    }

    /**
     * Runs an operator for each UI element that is rendered.
     * @param operator Operator to call
     * @return True if any operator cancelled execution (returned true)
     */
    public boolean forEachElement(final UiElementOperatorMethod operator) {
        for (final UiElement element : elements) {
            if (operator.run(element)) return true;
        }
        return false;
    }

    /**
     * Functional interface for UI elements.
     */
    public static interface UiElementOperatorMethod {
        /**
         * Runs an action on a UI element.
         * @param element Element to process
         * @return Whether to stop operating. If true is returned, any future objects down the list are not operated on.
         */
        public boolean run(final UiElement element);
    }
}
