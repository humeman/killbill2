package today.tecktip.killbill.frontend.ui;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Represents an element renderable by the {@link UiRenderer}.
 * @author cs
 */
public interface UiElement {
    /**
     * Sets the visibility state of this element.
     * @param visible True if visible
     */
    public void setVisible(final boolean visible);

    /**
     * Gets the visibility state of this element.
     * @return True if visible
     */
    public boolean isVisible();

    /**
     * Called each frame as the screen is rendered.
     * @param batch Sprite batch to render the element to
     * @param delta Time in seconds since last render
     */
    public void render(final SpriteBatch batch, float delta);

    /**
     * Called when this element is pressed.
     * @return True if input was processed
     */
    public boolean onClicked();

    /**
     * Called when this element is unpressed.
     * @return True if input was processed
     */
    public boolean onUnclicked();

    /**
     * Called when a key is pressed while the element is rendered.
     * <p>
     * This should be treated as a 'raw' method -- ie, for game controls.
     * If you want to take in user input as characters, use {@link #onKeyTyped(int)} instead.
     * @param key The key that was pressed (corresponding to GDX's {@link Keys} class)
     * @return True if input was processed
     */
    public boolean onKeyPressed(final int key);

    /**
     * Called when a character is typed while the element is rendered.
     * @param c The character that was typed
     * @return True if input was processed
     */
    public boolean onKeyTyped(final char c);

    /**
     * Gets the bounding box for this element.
     */
    public Rectangle getRectangle();

    /**
     * Called when the screen dimensions change.
     */
    public void onResize();
}
