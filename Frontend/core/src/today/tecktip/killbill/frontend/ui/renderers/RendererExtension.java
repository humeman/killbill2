package today.tecktip.killbill.frontend.ui.renderers;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import today.tecktip.killbill.frontend.ui.UiElement;

/**
 * An extension to the UiRenderer (a package of elements that can be
 *  added to or removed from a UiRenderer).
 * @author cs
 */
public abstract class RendererExtension {
    /**
     * The elements to add.
     */
    protected List<UiElement> elements;

    /**
     * Determines if this extension will be rendered or not.
     */
    private boolean isEnabled;

    /**
     * Creates an empty RendererExtension.
     */
    public RendererExtension(final boolean startState) {
        elements = new ArrayList<>();
        isEnabled = startState;
    }

    /**
     * Checks if this extension is enabled or not.
     * @return True if rendered
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Enables or disables this extension.
     * @param newState True = enabled, false = disabled
     */
    public void setEnabled(final boolean newState) {
        isEnabled = newState;
    }

    /**
     * Renders all elements to the UI.
     * @param batch Sprite batch to render to
     * @param delta Time in seconds since last render
     */
    public void render(final SpriteBatch batch, final float delta) {
        for (final UiElement element : elements) {
            if (element.isVisible())
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
        // Run in reverse so the topmost element is picked (in case of collisions)
        for (int i = elements.size() - 1; i >= 0; i--) {
            final UiElement element = elements.get(i);
            if (!element.isVisible()) continue;
            if (element.getRectangle() == null) continue;

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
        for (final UiElement element : elements) {
            element.onResize();
        }
    }
}
